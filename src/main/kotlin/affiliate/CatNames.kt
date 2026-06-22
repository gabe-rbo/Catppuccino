package dev.cdh.affiliate

/** Shared pet-name pool + uniqueness helpers, used for both live cats and the startup routine. */
object CatNames {
    val POOL = listOf(
        "Mochi", "Luna", "Simba", "Cleo", "Oreo", "Milo", "Nala", "Biscuit",
        "Pumpkin", "Shadow", "Ginger", "Pepper", "Coco", "Tigger", "Smokey",
        "Leo", "Bella", "Maple", "Hazel", "Pixel"
    )

    /** A pool name not in [used]; falls back to "Cat N" once the pool is exhausted. */
    fun randomUnused(used: Set<String>): String {
        POOL.filter { it !in used }.randomOrNull()?.let { return it }
        var i = used.size + 1
        while ("Cat $i" in used) i++
        return "Cat $i"
    }

    /** [desired] if free, otherwise "desired 2", "desired 3", … */
    fun unique(desired: String, used: Set<String>): String {
        if (desired !in used) return desired
        var i = 2
        while ("$desired $i" in used) i++
        return "$desired $i"
    }
}
