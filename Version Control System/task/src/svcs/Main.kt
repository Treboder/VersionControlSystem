package svcs

import java.io.File
import java.security.MessageDigest
import java.time.LocalDateTime

fun main(args: Array<String>) {

    // prepare directory/file structure
    Config.initFileSystem()

    // specify run mode, either in debugging mode or as desired with main args
    val debugging = false

    // desired usage with main args from command prompt
    if(!debugging)
        Interpreter.processArguments(args.toMutableList())

    // run interpreter in endless loop for easy debugging
    while(debugging) {
        print("> ")
        val arguments = mutableListOf<String>()
        arguments.addAll(readLine()!!.split(" "))
        Interpreter.processArguments(arguments)
    }
}

object Interpreter {

    fun processArguments(args: MutableList<String>) {
        if (args.size == 0) {
            help()
            return
        }

        if (args.size == 1)
            when (args[0]) {
                "--help" -> help()
                "config" -> config()
                "add" -> add()
                "log" -> log()
                "commit" -> commit()
                "checkout" -> checkout()
                else -> println("'${args[0]}' is not a SVCS command.")
            }

        if (args.size == 2)
            when (args[0]) {
                "--help" -> help()
                "config" -> config(args[1])
                "add" -> add(args[1])
                "log" -> log()
                "commit" -> commit(args[1])
                "checkout" -> checkout()
                else -> println("'${args[1]}' is not a SVCS command.")
            }
    }

    fun help() {
        println(
            "These are SVCS commands:\n" +
                    "config     Get and set a username.\n" +
                    "add        Add a file to the index.\n" +
                    "log        Show commit logs.\n" +
                    "commit     Save changes.\n" +
                    "checkout   Restore a file."
        )
    }

    fun config() {
        VCS.readUserFile()
        if(VCS.username == "")
            println("Please, tell me who you are.")
        else
            println("The username is ${VCS.username}.")
    }

    fun config(name: String) {
        VCS.changeUserAndSaveConfig(name)
        println("The username is ${VCS.username}.")
    }

    fun add() {
        VCS.readIndexFile()
        if(VCS.index.size == 0)
            println("Add a file to the index.")
        else {
            println("Tracked files:")
            for (file in VCS.index)
                println(file)
            // alternatively without loop in one line
            // println(repository.trackedFiles.joinToString(separator = CommonDefinitions.separator) {it})
        }
    }

    fun add(newFile:String) {
        val dir = System.getProperty("user.dir")
        val file = File(dir+"\\$newFile")
        //if(false) {
        if(!file.exists()) {
            println("Can't find '$newFile'.")
        }
        else {
            VCS.addFileToIndexAndSaveIndex(newFile)
            println("The file '$newFile' is tracked.")
        }
    }

    fun log() {
        VCS.showLog()
    }

    fun commit() {
        println("Message was not passed.")
    }

    fun commit(message:String) {
        val newCommit = Commit(message)
    }

    fun checkout() {
        println("Restore a file.")
    }

}

object VCS {

    var username = readUserFile()
    var log = readLogFile()
    var index = mutableListOf<String>()
    val indexItemSeparator = "\n"

    init {
        readIndexFile()
    }

    fun readUserFile():String {
        return Config.configFile.readText()
    }

    fun readLogFile():String {
        return Config.logFile.readText()
    }

    fun readIndexFile() {
        index.clear()
        for(fileName in Config.indexFile.readText().split(indexItemSeparator))
            if(fileName != "")
                index.add(fileName)
    }

    fun changeUserAndSaveConfig(user:String) {
        username = user
        Config.configFile.writeText(username)
    }

    fun addFileToIndexAndSaveIndex(fileName:String) {
        if(!index.contains(fileName)) {
            index.add(fileName)
            Config.indexFile.writeText(index.joinToString(separator = indexItemSeparator) { it })
        }
    }

    fun addEntryAndSaveLog(message:String, id:String) {
        // create new log entry
        var newlogEntry = "commit " + id + "\n"
        newlogEntry += "Author: " + VCS.username + "\n"
        newlogEntry += message + "\n"
        // append new log entry on top of the file and save to disk
        log = newlogEntry + "\n" + log
        Config.logFile.writeText(log)
    }

    fun showLog() {
        if (log != "")
            println(log)
        else
            println("No commits yet.")
    }
}

class  Commit(_message:String ) {

    val id = createHash(VCS.log + LocalDateTime.now().toString())

    init {
        if(filesChangedSinceLastCommit()) {
            VCS.addEntryAndSaveLog(_message, id)
            saveSnapshotToCommitDirectory()
            println("Changes are committed.")
        }
        else
            println("Nothing to commit.")
    }

    fun createHash(input: String):String {
        // hash a combination logfile and timestamp (optionally plus message)
        val bytes = input.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
        return ""
    }

    fun filesChangedSinceLastCommit(): Boolean {
        // fetch last commit id, and return true if no one exists
        var latestCommitID = "no commits so far"
        if(VCS.log != "")
            latestCommitID = VCS.log.lines()[0].split(" ")[1]
        else
            return true
        // compare hashes of all tracked files with those saved along with last commit
        for(fileName in VCS.index) {
            val currentFile = File(Config.rootDirectory.absolutePath + "\\" +fileName)
            val committedFile = File(Config.commitDirectory.absolutePath + "\\" + latestCommitID + "\\" +fileName)
            if(createHash(currentFile.readText()) != createHash(committedFile.readText()))
                return true
        }
        return false
    }

    fun saveSnapshotToCommitDirectory() {
        // create new dir with id
        val newCommitDir = File(Config.commitDirectory.absolutePath + "\\" + id)
        newCommitDir.mkdir()
        // copy all the tracked files
        for(fileName in VCS.index) {
            val sourceFile = File(Config.rootDirectory.absolutePath + "\\" +fileName)
            val targetFile = File(Config.commitDirectory.absolutePath + "\\" + id + "\\" +fileName)
            if(!targetFile.exists()) // should not exist
                sourceFile.copyTo(targetFile)
        }
    }

}

object Config {

    val rootDirectory = File(System.getProperty("user.dir"))
    val vcsDirectory = File(rootDirectory.absolutePath + "\\vcs")
    val commitDirectory = File(vcsDirectory.absolutePath + "\\commits")
    val configFile = File(vcsDirectory.absolutePath + "\\config.txt")
    var indexFile = File(vcsDirectory.absolutePath + "\\index.txt")
    var logFile = File(vcsDirectory.absolutePath + "\\log.txt")

    fun initFileSystem() {

        // create vcs directory
        if(!vcsDirectory.exists())
            vcsDirectory.mkdir()

        // create commit directory
        if(!commitDirectory.exists())
            commitDirectory.mkdir()

        // create config file
        if(!configFile.exists())
            configFile.createNewFile()

        // create index file
        if(!indexFile.exists())
            indexFile.createNewFile()

        // create log file
        if(!logFile.exists())
            logFile.createNewFile()

    }
}
