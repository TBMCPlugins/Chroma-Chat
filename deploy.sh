#!/bin/sh
FILENAME=$(find target/ -maxdepth 1 ! -name '*original*' -name '*.jar')
echo Found file: $FILENAME

if [ $1 = 'production' ]; then
echo Production mode
echo $UPLOAD_KEY > upload_key
chmod 400 upload_key
yes | scp -B  -i upload_key -o StrictHostKeyChecking=no $FILENAME travis@server.figytuna.com:/minecraft/main/plugins
fi
