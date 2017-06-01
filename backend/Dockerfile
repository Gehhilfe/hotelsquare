FROM node:7.10.0-alpine
ENV NODE_ENV staging

# Create app directory
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

COPY package.json /usr/src/app
RUN npm install --production --silent

COPY . /usr/src/app

EXPOSE 8081

CMD [ "npm", "start" ]
