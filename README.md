= FuzRec

Run experiments on collected dataset 

```bash
./gradlew runners:run --args="-i /dir_with_yaml_files -o /output_dir"
```

If you want to run project from IDEA then add this `VM options` in task configuration

```
--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED 
--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED 
--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED 
--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED
```