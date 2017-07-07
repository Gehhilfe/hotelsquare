'use strict';

const mongoose = require('mongoose');
const Schema = mongoose.Schema;
const uuidv1 = require('uuid/v1');

const config = require('config');
const minio = require('minio');
const minioClient = new minio.Client({
    endPoint: 'stimi.ovh',
    port: 9000,
    secure: false,
    accessKey: config.minio.key,
    secretKey: config.minio.secret
});
const sharp = require('sharp');
const URLSafeBase64 = require('urlsafe-base64');
const ExifImage = require('exif').ExifImage;

// ---------------------------------------------------------------------------------------------------------------------
// Schema
// ---------------------------------------------------------------------------------------------------------------------

const ImageSchema = new Schema({
    uuid: {
        type: Buffer,
        required: true,
        index: {
            unique: true
        },
        default: () => {
            const arr = [];
            uuidv1(null, arr, 0);
            return Buffer.from(arr);
        }
    },
    created_at: {
        type: Date,
        default: Date.now()
    },
    uploader: {
        type: Schema.Types.ObjectId,
        ref: 'User'
    },
    location: {
        'type': {type: String, default: 'Point'},
        coordinates: {
            type: [Number], default: [0, 0]
        }
    }
});

ImageSchema.index({location: '2dsphere'});

// ---------------------------------------------------------------------------------------------------------------------
// Class
// ---------------------------------------------------------------------------------------------------------------------

class ImageClass {

    /**
     * Extracts exif information from image
     * @param {String} path Path to images
     * @returns {Promise.Object} ExifData see npm exif
     * @private
     */
    static _getExifInformation(path) {
        return new Promise((resolve, reject) => {
            new ExifImage({
                image: path
            }, (err, data) => {
                if (err)
                    reject(err);
                else
                    resolve(data);
            });
        });
    }


    /**
     * Uploads jpeg buffer onto cloud storage
     * @param {Buffer} buffer Buffer containing jpeg image
     * @param {String} fileName Name of image on cloud storage
     * @returns {Promise} ETag of cloud storage instance
     * @private
     */
    static _uploadImage(buffer, fileName) {
        return new Promise((resolve, reject) => {
            minioClient.putObject(config.minio.bucket, fileName, buffer, 'image/jpeg', (err, etag) => {
                if (err)
                    reject(err);
                else
                    resolve(etag);
            });
        });
    }

    /**
     * Trys to remove image from cloud storage without throwing errors
     * @param {String} fileName Name of image on cloud storage
     * @returns {Promise} Null or error
     * @private
     */
    static _tryRemoveImage(fileName) {
        return new Promise((resolve) => {
            minioClient.removeObject(config.minio.bucket, fileName, (err) => {
                resolve(err);
            });
        });
    }


    /**
     * Stores a image into cloud storage and creates a corresponding document
     *
     * @param {String} path image buffer
     * @param {User|null} user uploader
     * @returns {ImageClass} created image
     */
    static async upload(path, user = null) {
        const self = this;
        const img = new self();
        img.uploader = user;

        // const exif = await this._getExifInformation(path);

        // Resize images
        const [small, middle, large] = await Promise.all([
            sharp(path)
                .resize(200, 200)
                .max()
                .toFormat('jpeg')
                .toBuffer(),
            sharp(path)
                .resize(500, 500)
                .max()
                .toFormat('jpeg')
                .toBuffer(),
            sharp(path)
                .resize(1920, 1080)
                .max()
                .toFormat('jpeg')
                .toBuffer()
        ]);
        const baseFileName = URLSafeBase64.encode(img.uuid);

        try {
            // Upload images onto cloud storage
            await  Promise.all([
                this._uploadImage(small, baseFileName + '_' + 'small.jpeg'),
                this._uploadImage(middle, baseFileName + '_' + 'middle.jpeg'),
                this._uploadImage(large, baseFileName + '_' + 'large.jpeg')
            ]);
            // Images a stored
            return await img.save();
        } catch (err) {
            // Error happened try to clean up storage
            await  Promise.all([
                this._tryRemoveImage(baseFileName + '_' + 'small.jpeg'),
                this._tryRemoveImage(baseFileName + '_' + 'middle.jpeg'),
                this._tryRemoveImage(baseFileName + '_' + 'large.jpeg')
            ]);
        }
    }

    /**
     * @returns {[String,String,String]} filenames for small, middle and large
     */
    filenames() {
        const baseFileName = URLSafeBase64.encode(this.uuid);
        return [baseFileName + '_' + 'small.jpeg',
            baseFileName + '_' + 'middle.jpeg',
            baseFileName + '_' + 'large.jpeg'];
    }

    /**
     * Deletes all stored images and removes instance from database
     * @param {ImageClass} img image to destory
     * @returns {Promise} remove image instance
     */
    static async destroy(img) {
        const baseFileName = URLSafeBase64.encode(img.uuid);
        await  Promise.all([
            this._tryRemoveImage(baseFileName + '_' + 'small.jpeg'),
            this._tryRemoveImage(baseFileName + '_' + 'middle.jpeg'),
            this._tryRemoveImage(baseFileName + '_' + 'large.jpeg')
        ]);
        return await img.remove();
    }
}

ImageSchema.loadClass(ImageClass);
module.exports = mongoose.model('Image', ImageSchema);