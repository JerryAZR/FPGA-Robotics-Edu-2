find . -name "*\.java" | xargs grep -l 'Log\.' | xargs sed -i 's/Log\./;\/\/ Log\./g'
