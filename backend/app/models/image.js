'use strict';
const _ = require('lodash');

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
    assigned: {
        kind: String,
        to: {
            type: Schema.Types.ObjectId,
            refPath: 'assigned.kind'
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

    assignTo(assigned_to) {
        this.assigned.to = assigned_to;
        this.assigned.kind = assigned_to.constructor.modelName;
    }

    /**
     * Stores a image into cloud storage and creates a corresponding document
     *
     * @param {String} path image buffer
     * @param {User|null} user uploader
     * @param {Object} assigned_to model image is assigned to
     * @returns {Promise} created image
     */
    static async upload(path, user = null, assigned_to = null) {
        const self = this;
        const img = new self();
        img.uploader = user;
        if (assigned_to) {
            img.assignTo(assigned_to);
        }
        // const exif = await this._getExifInformation(path);

        // Resize images
        const buffers = await Promise.all([
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
        const small = buffers[0];
        const middle = buffers[1];
        const large = buffers[2];
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
        return img.remove();
    }

    toJSON() {
        const obj = this.toObject({
            depopulate: true
        });
        return obj;
    }

    get baseFileName() {
        return URLSafeBase64.encode(this.uuid);
    }

    _getUrl(postfix) {
        return new Promise((resolve, reject) => {
            minioClient.presignedGetObject(config.minio.bucket, this.baseFileName + '_' + postfix + '.jpeg',
                24 * 60 * 60, function (err, presignedUrl) {
                    if (err)
                        reject(err);
                    else
                        resolve(presignedUrl);
                });
        });
    }

    smallUrl() {
        return this._getUrl('small');
    }

    middleUrl() {
        return this._getUrl('middle');
    }

    largeUrl() {
        return this._getUrl('large');
    }

    /**
     * Get object stat
     * @param {Number} size 0 to 2 small to large
     * @returns {Promise} stat
     */
    getStat(size) {
        return new Promise((resolve, reject) => {
            minioClient.statObject(config.minio.bucket, this.filenames()[size], (err, stat) => {
                if (err)
                    reject(err);
                else
                    resolve(stat);
            });
        });
    }

    getObject(size) {
        return new Promise((resolve, reject) => {
            minioClient.getObject(config.minio.bucket, this.filenames()[size], (err, datastream) => {
                if (err)
                    reject(err);
                else {
                    let buffer = Buffer.alloc(0);
                    datastream.on('data', (chunk) => {
                        buffer = Buffer.concat([buffer, chunk]);
                    });
                    datastream.on('end', () => {
                        resolve(buffer);
                    });
                    datastream.on('error', (e) => {
                        reject(e);
                    });
                }
            });
        });
    }
}

ImageSchema.loadClass(ImageClass);
module.exports = mongoose.model('Image', ImageSchema);