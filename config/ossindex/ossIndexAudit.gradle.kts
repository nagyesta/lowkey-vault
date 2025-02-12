buildscript {
    fun readExclusions(): MutableSet<String> {
        return rootProject.file("config/ossindex/exclusions.txt").readLines()
                .stream()
                .toList()
                .filter { it.isNotBlank() }
                .toMutableSet()
    }

    extra.set("ossIndexExclusions", readExclusions())
}
