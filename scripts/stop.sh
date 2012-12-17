kill -9 $(ps auxww |grep play |grep java | tr -d "\t" | tr -s ' ' | cut -f2 -d ' ')
