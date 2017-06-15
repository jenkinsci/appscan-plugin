rm -Rf messages
find . -name "*en.properties" -exec ditto {} messages/{} \;
find . -name "*essages.properties" -exec ditto {} messages/{} \;
find messages -name "*.properties" -exec perl -pi.bak -e 's/\r\n|\n|\r/\r\n/g' {} \;
find . -name "*.bak" -type f -exec rm {} \;
