package svcs

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

fun main(args: Array<String>) {

    val debugging = false
    val settings = Settings()
    val repository = Repository()
    val interpreter = Interpreter(settings, repository)

    // run interpreter in a loop for easy debugging
    while(debugging) {
        print("> ")
        val arguments = mutableListOf<String>()
        arguments.addAll(readLine()!!.split(" "))
        interpreter.processArguments(arguments)
        if(arguments.first() == "exit")
            return
    }

    // desired usage with main args from command prompt
    if(!debugging)
        interpreter.processArguments(args.toMutableList())

}

class Interpreter(_settings:Settings, _repository:Repository) {

    val settings = _settings
    val repository = _repository

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
        settings.loadSettings()
        if(settings.username == "")
            println("Please, tell me who you are.")
        else
            println("The username is ${settings.username}.")
    }

    fun config(name: String) {
        settings.username = name
        settings.saveSettings()
        println("The username is ${settings.username}.")
    }

    fun add() {
        repository.loadSettings()
        if(repository.trackedFiles.size == 0)
            println("Add a file to the index.")
        else {
            println("Tracked files:")
            for (file in repository.trackedFiles)
                println(file)
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
            repository.trackedFiles.add(newFile)
            repository.saveSettings()
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

class Settings() {

    val dir = System.getProperty("user.dir") + "\\vcs"
    val file = File(dir+"\\config.txt")
    var username = ""

    init {
        initFileSystem()
        loadSettings()
    }

    fun initFileSystem() {
        // create directory
        if(!Files.exists(Paths.get(dir)))
            Files.createDirectory(Paths.get(dir))
        // create file
        if(!file.exists())
            file.createNewFile()
    }

    fun loadSettings() {
        username = file.readText()
    }

    fun saveSettings() {
        file.writeText(username)
    }
}

class Repository() {

    val dir = System.getProperty("user.dir") + "\\vcs"
    var file = File(dir+"\\index.txt")
    var trackedFiles = mutableListOf<String>()

    init {
        initFileSystem()
        loadSettings()
    }

    fun initFileSystem() {
        // create directory
        if(!Files.exists(Paths.get(dir)))
            Files.createDirectory(Paths.get(dir))
        // create file
        if(!file.exists())
            file.createNewFile()
    }

    fun loadSettings() {
        trackedFiles.clear()
        for(fileName in file.readText().split(" "))
            if(fileName != "")
                trackedFiles.add(fileName)
    }

    fun saveSettings() {
        file.writeText(trackedFiles.joinToString(separator = " ") {it})
    }

}