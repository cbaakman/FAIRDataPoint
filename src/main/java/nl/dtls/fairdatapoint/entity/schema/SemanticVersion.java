package nl.dtls.fairdatapoint.entity.schema;

import lombok.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

import static java.lang.String.format;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder(toBuilder = true)
public class SemanticVersion implements Comparable<SemanticVersion> {

    private int major;

    private int minor;

    private int patch;

    public String toString() {
        return format("%d.%d.%d", major, minor, patch);
    }

    public SemanticVersion(String semverString) {
        var parts = Arrays.stream(semverString.split("\\.")).map(Integer::parseInt).collect(Collectors.toList());
        major = parts.get(0);
        minor = parts.get(1);
        patch = parts.get(2);
    }

    @Override
    public int compareTo(SemanticVersion other) {
        return Comparator.comparing(SemanticVersion::getMajor)
                .thenComparing(SemanticVersion::getMinor)
                .thenComparing(SemanticVersion::getPatch)
                .compare(this, other);
    }
}
