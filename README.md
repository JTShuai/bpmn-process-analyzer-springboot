<!--
 * @Author: Jiangtao Shuai
 * @Date: 2023-07-09 15:39:56
 * @Description: 
 * 
-->
# bpmn-process-analyzer-springboot
Backend software with SpringBoot.
1. Read bpmn diagram and generate all possible processes.
2. Use a defined process to generate a bpmn file.


## Example
A `bpmn` process [diagram](./doc/demo_process.bpmn):
<img src=./doc/diagram.png alt="Alt Text" width="600" style="border: 1px solid black;">

Result:
```
"status": "OK",
"content": {
    "A-B-D-C-E-Y-X-G": "A-B-D-C-E-Y-X-G",
    "A-B-C-E-X-Y-D-G": "A-B-C-E-X-Y-D-G",
    "A-B-C-F-D-G": "A-B-C-F-D-G",
    "A-B-C-E-Y-X-D-G": "A-B-C-E-Y-X-D-G",
    "A-B-D-C-E-X-Y-G": "A-B-D-C-E-X-Y-G",
    "A-B-D-C-F-G": "A-B-D-C-F-G"
}
```