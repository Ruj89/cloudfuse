#!/usr/bin/env bash
curl \
    --request GET \
    "https://www.googleapis.com/drive/v3/files?q=name+=+'cloudfuse'" \
    -H "Authorization: Bearer ya29.Ci_TAxPHHz6fdHTiUG-F6XP2Aa-7Pr91t8c6WhoeoxkakUtYBqurQTodFO_boEZmew" \
    -v