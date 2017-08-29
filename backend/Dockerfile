FROM node:7.10.0
ENV NODE_ENV staging

# Set timezone
RUN ln -fs /usr/share/zoneinfo/Europe/Berlin /etc/localtime

# Create app directory
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

COPY package.json /usr/src/app
RUN npm install --production --silent

COPY . /usr/src/app

EXPOSE 8081

CMD [ "npm", "start" ]
