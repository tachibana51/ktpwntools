kotlinc *.kt -include-runtime -d %1.jar -cp .
kotlin %1.jar
del %1.jar
