package svcs

fun main(args: Array<String>) {

    if(args.size == 0) {
        help()
        return
    }

    when(args[0]) {
        "--help" -> help()
        "config" -> config()
        "add" -> add()
        "log" -> log()
        "commit" -> commit()
        "checkout" -> checkout()
        else -> println("'${args[0]}' is not a SVCS command.")
    }

}

fun help() {
    println("These are SVCS commands:\n" +
            "config     Get and set a username.\n" +
            "add        Add a file to the index.\n" +
            "log        Show commit logs.\n" +
            "commit     Save changes.\n" +
            "checkout   Restore a file.")
}

fun config(){
    println("Get and set a username.")
}

fun add() {
    println("Add a file to the index.")
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