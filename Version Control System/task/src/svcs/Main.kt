package svcs

import java.io.File
import java.security.MessageDigest
import java.time.LocalDateTime

fun main(args: Array<String>) {

    // prepare directory/file structure
    StaticAppConfig.initFileSystem()

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
        Config.loadConfig()
        if(Config.username == "")
            println("Please, tell me who you are.")
        else
            println("The username is ${Config.username}.")
    }

    fun config(name: String) {
        Config.username = name
        Config.saveConfig()
        println("The username is ${Config.username}.")
    }

    fun add() {
        Index.loadIndex()
        if(Index.trackedFiles.size == 0)
            println("Add a file to the index.")
        else {
            println("Tracked files:")
            for (file in Index.trackedFiles)
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
            Index.addFileToIndexAndSaveIndex(newFile)
            println("The file '$newFile' is tracked.")
        }
    }

    fun log() {
        Log.showLog()
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

object Config {

    var username = loadConfig()

    fun loadConfig():String {
        return StaticAppConfig.configFile.readText()
    }

    fun saveConfig() {
        StaticAppConfig.configFile.writeText(username)
    }
}

object Index {

    var trackedFiles = mutableListOf<String>()
    val indexItemSeparator = "\n"

    init {
        loadIndex()
    }

    fun loadIndex() {
        trackedFiles.clear()
        for(fileName in StaticAppConfig.indexFile.readText().split(indexItemSeparator))
            if(fileName != "")
                trackedFiles.add(fileName)
    }

    fun addFileToIndexAndSaveIndex(fileName:String) {
        if(!trackedFiles.contains(fileName)) {
            Index.trackedFiles.add(fileName)
            StaticAppConfig.indexFile.writeText(trackedFiles.joinToString(separator = indexItemSeparator) { it })
        }
    }
}

object Log {

    var log = loadLog()

    fun loadLog():String {
        return StaticAppConfig.logFile.readText()
    }

    fun addEntryAndSaveLog(message:String, id:String) {
        // create new log entry
        var newlogEntry = "commit " + id + "\n"
        newlogEntry += "Author: " + Config.username + "\n"
        newlogEntry += message + "\n"
        // append new log entry on top of the file and save to disk
        log = newlogEntry + "\n" + log
        StaticAppConfig.logFile.writeText(log)
    }

    fun showLog() {
        if (log != "")
            println(log)
        else
            println("No commits yet.")
    }

}

class  Commit(_message:String ) {

    val id = createHash(Log.log + LocalDateTime.now().toString())

    init {
        if(filesChangedSinceLastCommit()) {
            Log.addEntryAndSaveLog(_message, id)
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
        if(Log.log != "")
            latestCommitID = Log.log.lines()[0].split(" ")[1]
        else
            return true
        // compare hashes of all tracked files with those saved along with last commit
        for(fileName in Index.trackedFiles) {
            val currentFile = File(StaticAppConfig.rootDirectory.absolutePath + "\\" +fileName)
            val committedFile = File(StaticAppConfig.commitDirectory.absolutePath + "\\" + latestCommitID + "\\" +fileName)
            if(createHash(currentFile.readText()) != createHash(committedFile.readText()))
                return true
        }
        return false
    }

    fun saveSnapshotToCommitDirectory() {
        // create new dir with id
        val newCommitDir = File(StaticAppConfig.commitDirectory.absolutePath + "\\" + id)
        newCommitDir.mkdir()
        // copy all the tracked files
        for(fileName in Index.trackedFiles) {
            val sourceFile = File(StaticAppConfig.rootDirectory.absolutePath + "\\" +fileName)
            val targetFile = File(StaticAppConfig.commitDirectory.absolutePath + "\\" + id + "\\" +fileName)
            if(!targetFile.exists()) // should not exist
                sourceFile.copyTo(targetFile)
        }
    }

}

object StaticAppConfig {

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
