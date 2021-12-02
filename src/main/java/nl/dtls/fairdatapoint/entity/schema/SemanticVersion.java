/**
 * The MIT License
 * Copyright © 2017 DTL
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
