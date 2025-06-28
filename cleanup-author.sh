#!/bin/bash

CORRECT_NAME="Zarnab Shaheen"
CORRECT_EMAIL="zarnaabshaheen@gmail.com"

git filter-branch --env-filter "
export GIT_AUTHOR_NAME='$CORRECT_NAME'
export GIT_AUTHOR_EMAIL='$CORRECT_EMAIL'
export GIT_COMMITTER_NAME='$CORRECT_NAME'
export GIT_COMMITTER_EMAIL='$CORRECT_EMAIL'
" --tag-name-filter cat -- --branches --tags
