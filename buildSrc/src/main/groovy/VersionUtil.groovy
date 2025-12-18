import org.gradle.api.GradleException

class VersionUtil {
    static boolean versionMatches(String rule, String mcVersion) {
        mcVersion = normalize(mcVersion)
        rule = rule.trim()

        if (!rule.contains("*") &&
                !rule.contains("x") &&
                !rule.contains("+") &&
                !rule.contains("-") &&
                !rule.contains("{") &&
                !rule.contains("}")) {
            return normalize(rule) == mcVersion
        }

        if (rule.endsWith(".*") || rule.endsWith(".x")) {
            def prefix = rule[0..<(rule.lastIndexOf('.'))]
            return mcVersion.startsWith(prefix)
        }

        if (rule == "*" || rule == "x" || rule == "*.x" || rule == "*.*") {
            return true
        }

        if (rule.endsWith("+")) {
            def base = normalize(rule[0..-2])
            return semverCompare(mcVersion, base) >= 0
        }

        // compound rule "A}{B"
        if (rule.contains("}{")) {
            def (left, right) = rule.split("\\}\\{")

            left = left.trim()
            right = right.trim()

            // left side: >, >=
            def leftRule = left.startsWith("{=") || left.startsWith("{")
                    ? left
                    : "{${left}"

            // right side: <, <=
            def rightRule = right.startsWith("}=") || right.startsWith("}")
                    ? right
                    : "}${right}"

            return versionMatches(leftRule, mcVersion) &&
                    versionMatches(rightRule, mcVersion)
        }



        if (rule.startsWith("{=")) {
            return semverCompare(mcVersion, normalize(rule.substring(2).trim())) >= 0
        }
        if (rule.startsWith("}=")) {
            return semverCompare(mcVersion, normalize(rule.substring(2).trim())) <= 0
        }
        if (rule.startsWith("{")) {
            return semverCompare(mcVersion, normalize(rule.substring(1).trim())) > 0
        }
        if (rule.startsWith("}")) {
            return semverCompare(mcVersion, normalize(rule.substring(1).trim())) < 0
        }

        if (rule.contains("-")) {
            def (start, end) = rule.split("-")
            start = normalize(start.trim())
            end = normalize(end.trim())
            return semverCompare(mcVersion, start) >= 0 &&
                    semverCompare(mcVersion, end) <= 0
        }


        return false
    }

    static String normalize(String v) {
        def parts = v.split(/\./) as List
        while (parts.size() < 3) parts << "0"
        return parts.take(3).join(".")
    }

    static int semverCompare(String a, String b) {
        def ap = a.split(/\./)*.toInteger()
        def bp = b.split(/\./)*.toInteger()
        for (int i = 0; i < 3; i++) {
            if (ap[i] != bp[i]) return ap[i] <=> bp[i]
        }
        return 0
    }

    static File getDirectory(String rootDir, String mcVersion){
        def versionsDir = new File("${rootDir}/versions")

        def matchingFolders = versionsDir.listFiles()?.findAll { dir ->
            dir.isDirectory() && versionMatches(dir.name, mcVersion)
        } ?: []

        if (matchingFolders.isEmpty()) {
            throw new GradleException("No matching version folder found for ${mcVersion}")
        }

        return matchingFolders[0];
    }

    static String[] getVersionsFromDirectory(String rootDir, File directory) {
        def rule = directory.name.trim()

        // Handle single version
        if (!rule.contains("*") &&
                !rule.contains("x") &&
                !rule.contains("+") &&
                !rule.contains("-") &&
                !rule.contains("<") &&
                !rule.contains(">")) {
            return [normalize(rule)]
        }

        // Handle wildcard 1.21.* or 1.21.x
        if (rule.endsWith(".*") || rule.endsWith(".x")) {
            def prefix = rule[0..<(rule.lastIndexOf('.'))]
            def versions = []
            // generate patch 0..99 for this minor version
            for (int patch = 0; patch <= 99; patch++) {
                versions << "${prefix}.${patch}"
            }
            return versions as String[]
        }

        // Handle open-ended plus: 1.21.10+
        if (rule.endsWith("+")) {
            def base = normalize(rule[0..-2])
            def baseParts = base.split(/\./)*.toInteger()
            def versions = []

            // generate versions starting from base patch 0..99 for simplicity
            for (int patch = baseParts[2]; patch <= 99; patch++) {
                versions << "${baseParts[0]}.${baseParts[1]}.${patch}"
            }
            return versions as String[]
        }

        // Handle ranges 1.21.7-1.21.10
        if (rule.contains("-")) {
            def (start, end) = rule.split("-")
            start = normalize(start.trim())
            end = normalize(end.trim())

            def startParts = start.split(/\./)*.toInteger()
            def endParts = end.split(/\./)*.toInteger()

            def versions = []
            int major = startParts[0]
            int minor = startParts[1]
            for (int patch = startParts[2]; patch <= endParts[2]; patch++) {
                versions << "${major}.${minor}.${patch}"
            }
            return versions as String[]
        }

        // Handle compound rules like "1.21.6}{1.21.8" meaning ">1.21.6 AND <1.21.8"
        if (rule.contains("}{")) {
            def (left, right) = rule.split("\\}\\{")
            left = left.trim()
            right = right.trim()

            def versions = []
            // brute force minor patch range 0..99
            for (int major = 1; major <= 2; major++) {
                for (int minor = 0; minor <= 30; minor++) {
                    for (int patch = 0; patch <= 99; patch++) {
                        def v = "${major}.${minor}.${patch}"
                        if (versionMatches("{${left}", v) && versionMatches("}${right}", v)) {
                            versions << v
                        }
                    }
                }
            }
            return versions as String[]
        }


        // For other rules like >, >=, <, <= or *
        // fallback: just return the normalized rule as a single version (cannot enumerate)
        return [normalize(rule)]
    }

}
