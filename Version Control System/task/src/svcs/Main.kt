package svcs

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

fun main(args: Array<String>) {

    // prepare directory/file structure
    StaticAppConfig.initFileSystem()

    // init global variables
    val config = Config()
    val index = Index()
    val interpreter = Interpreter(config, index)

    // specify run mode, either in debugging mode or as desired with main args
    val debugging = false

    // desired usage with main args from command prompt
    if(!debugging)
        interpreter.processArguments(args.toMutableList())

    // run interpreter in endless loop for easy debugging
    while(debugging) {
        print("> ")
        val arguments = mutableListOf<String>()
        arguments.addAll(readLine()!!.split(" "))
        interpreter.processArguments(arguments)
    }
}

class Interpreter(_config:Config, _index:Index) {

    val config = _config
    val index = _index

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
                "commit" -> commit()
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
        config.loadConfig()
        if(config.username == "")
            println("Please, tell me who you are.")
        else
            println("The username is ${config.username}.")
    }

    fun config(name: String) {
        config.username = name
        config.saveConfig()
        println("The username is ${config.username}.")
    }

    fun add() {
        index.loadIndex()
        if(index.trackedFiles.size == 0)
            println("Add a file to the index.")
        else {
            println("Tracked files:")
            for (file in index.trackedFiles)
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
            index.trackedFiles.add(newFile)
            index.saveIndex()
            println("The file '$newFile' is tracked.")
        }
    }

    fun log() {
        println("Show commit logs.")
    }

    fun commit() {
        println("Save changes.")
    }

    fun checkout() {
        println("Restore a file.")
    }

}

class Config() {

    var username = ""

    init {
        loadConfig()
    }

    fun loadConfig() {
        username = StaticAppConfig.configFile.readText()
    }

    fun saveConfig() {
        StaticAppConfig.configFile.writeText(username)
    }
}

class Index() {

    var trackedFiles = mutableListOf<String>()

    init {
        loadIndex()
    }

    fun loadIndex() {
        trackedFiles.clear()
        for(fileName in StaticAppConfig.indexFile.readText().split(StaticAppConfig.indexItemSeparator))
            if(fileName != "")
                trackedFiles.add(fileName)
    }

    fun saveIndex() {
        StaticAppConfig.indexFile.writeText(trackedFiles.joinToString(separator = StaticAppConfig.indexItemSeparator) {it})
    }
}

object StaticAppConfig {

    val vcsDirectory = System.getProperty("user.dir") + "\\vcs"
    val configFile = File(vcsDirectory + "\\config.txt")
    var indexFile = File(vcsDirectory + "\\index.txt")
    val indexItemSeparator = "\n"

    fun initFileSystem() {

        // create directory
        if(!Files.exists(Paths.get(vcsDirectory)))
            Files.createDirectory(Paths.get(vcsDirectory))

        // create config file
        if(!configFile.exists())
            configFile.createNewFile()

        // create index file
        if(!indexFile.exists())
            indexFile.createNewFile()
    }
}