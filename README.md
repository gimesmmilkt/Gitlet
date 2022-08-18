# Gitlet
Implment a version control system by Java.
## Overview
This is a project from [CS61B](https://sp21.datastructur.es/materials/proj/proj2/proj2). Gitlet mimics some fundamental functions in the Git, but in a simple way.  
In general, Gitlet support following functions:  
_1.Saving files, called commiting and commits hold the content of files._  
_2.Restore the files, called check out._  
_3.Display all commits with each information, called log._  
_4.Generate a new sequence of commits, called branch._  
_5.Merge two sequences into one, called merge._
## Commands
Be sure to use `init` to initialize the programe.  
```
java gitlet.Main init 
```
Create an initial commit as the start of all other commits.
```
java gitlet.Main add [file name]
```
Add a copy of file to the staging area and wait to be commited or removed.
```
java gitlet.Main commit [message]
```
Create a new commit to store the files in the staging area, also includes the time and message.
```
java gitlet.Main rm [file name]
```
Unstage the file in the staging area.
```
java gitlet.Main log
```
Display all commits in one branch and their information, from the current commit to the initial commit.
```
java gitlet.Main global-log
```
Display all commits in all branches, the order does not matter.
```
java gitlet.Main find [commit message]
```
Find the commit by the given message.
```
java gitlet.Main status
```
Display branches existed and current branch will be marked by *, also display the files in staging area.
```
java gitlet.Main checkout -- [file name]
java gitlet.Main checkout [commit id] -- [file name]
java gitlet.Main checkout [branch name]
```
The first command check out the file in the current commit.  
The second command check out the file in the given commit.  
The third command check out the spcific branch. 
```
java gitlet.Main branch [branch name]
```
Create a new branch, split from the current commit.
```
java gitlet.Main rm-branch [branch name]
```
Remove the branch with given branch name.
```
java gitlet.Main reset [commit id]
```
Check out all files in the given commmit.
```
java gitlet.Main merge [branch name]
```
Merge the files from the given branch into the current branch.
## Persistence
Since this is a version control system, the persistence is necessary. In general, serialization is used to store essential objects.  
Specifically, staging area is divided into addition and removal to store files. 
