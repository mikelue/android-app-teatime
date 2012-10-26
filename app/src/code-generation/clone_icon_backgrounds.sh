#!/bin/bash

function generateDrawableOfIconBackground()
{
	DEST_FILE="../../res/drawable/icon_background_${1}.xml"

	cp ../../res/drawable/icon_background_1.xml $DEST_FILE
	sed -i -e "s/type_1/type_${1}/g" $DEST_FILE
	echo "Generated $DEST_FILE"
}

cd "$( dirname "${BASH_SOURCE[0]}" )"

generateDrawableOfIconBackground 2
generateDrawableOfIconBackground 3
generateDrawableOfIconBackground 4
generateDrawableOfIconBackground 5
generateDrawableOfIconBackground 6
generateDrawableOfIconBackground 7
generateDrawableOfIconBackground 8
