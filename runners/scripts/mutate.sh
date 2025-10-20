dataset="RxJava-2.2.2"
cd ../..
./gradlew :errecfuzz:app:run --args="-i /home/olga/src/dataset/clear/$dataset -o /home/olga/src/dataset/grigra/mutated/$dataset -d $dataset -n 25 -r"
