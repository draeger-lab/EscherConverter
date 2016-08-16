#!/usr/bin/sh
# EscherConverter

rm -rf public/
brunch b -p
cp -r public/ ../api/static/
