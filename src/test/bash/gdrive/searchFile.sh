#!/usr/bin/env bash
curl \
    --request GET \
    "https://www.googleapis.com/drive/v3/files?q=trashed+=+false+and+name+=+'cloudfuse'+and+'root'+in+parents+and+mimeType+=+'application/vnd.google-apps.folder'" \
    -H "Authorization: Bearer ya29.Ci_TA5lhAA3ZNrcOiXqT-Hb1AY8Rqd0YfWIgbd-ppqCg-EBlnCpQU0kXMPypsJ7GOA" \
    -v