#!/usr/bin/env bash
curl \
    --request PATCH \
    "https://www.googleapis.com/drive/v3/files/0B6SQhclX7loWazlvNmRfanRlMEE?addParents=0B6SQhclX7loWSFBTLUxTdll2VEU" \
    --data '{"name":"prova.txt","mimeType":"application/octet-stream"}' \
    -H "Content-Type: application/json" \
    -H "Host: www.googleapis.com" \
    -H "Connection: Keep-Alive" \
    -H "User-Agent: Apache-HttpClient/4.3.4" \
    -H "Authorization: Bearer ya29.Ci_TA7aX0YF0EDPposevvaWINB42D06wgWeKhxzHj9bXC681UMZGfKhHK-K2XKk-8w" \
    -v