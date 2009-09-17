(function() { // To limit the scope of the private variables

    /**
     * @author dom.derrien
     * @maintainer dom.derrien
     */
    var module = dojo.provide("domderrien.utils.StringUtils");

    /** Helper returning <code>true</code> if the given character is an digit, <code>false</code> otherwise.
     *  @return <code>true</code> if the character is a digit
     *  @type Boolean
     *  @public
     *  @static
     */
    module.isDigit = function(character) {
        if (character == null) {
            return false;
        }
        if (typeof character == 'number') {
            return true;
        }
        if (typeof character != 'string') {
            return false;
        }
        if (1 < character.length) {
            return false;
        }
        var charCode = character.charCodeAt(0);
        return (48 <= charCode && charCode <= 57);
    };

    /** Helper returning <code>true</code> if the given character is a valid hexadecimal digit, <code>false</code> otherwise.
     *  @return <code>true</code> if the character is a hexadecimal digit
     *  @type Boolean
     *  @public
     *  @static
     */
    module.isHexaDigit = function(character) {
        if (character == null || 1 < character.length) {
            return false;
        }
        var charCode = character.charCodeAt(0);
        return (48 <= charCode && charCode <= 57) || (65 <= charCode && charCode <= 70) || (97 <= charCode && charCode <= 102);
    };

    /** Helper returning <code>true</code> if the given character is a non-alphanumeric character, <code>false</code> otherwise.
     *  @return <code>true</code> if the character is a non-alphanumeric character
     *  @type Boolean
     *  @public
     *  @static
     */
    module.isNonAlpha = function(character) {
        if (character == null || 1 < character.length) {
            return true;
        }
        var charCode = character.charCodeAt(0);
        if (charCode < 48) return true; // Before '0'
        if (122 < charCode) return true; // After 'z'
        if (57 < charCode && charCode < 65) return true; // Between '9' and 'A'
        if (90 < charCode && charCode < 97) return true; // Between 'Z' and 'a'
        return false;
    };

    /** Transcript the given number in its hexadecimal string representation
     *  @param {Number} number data to be converted
     *  @return Hexadecimal representation
     *  @type String
     *  @static
     */
    module.convertToHexa = function(number) {
        number = Number(number);
        if (isNaN(number)) {
            return "";
        }
        var output = number == 0 ? "0" : "";
        while (1 <= number) {
            var digit = number % 16;
            switch(digit) {
                case 10: output = 'a' + output; break;
                case 11: output = 'b' + output; break;
                case 12: output = 'c' + output; break;
                case 13: output = 'd' + output; break;
                case 14: output = 'e' + output; break;
                case 15: output = 'f' + output; break;
                default: output = digit + output;
            }
            number = Math.floor(number / 16);
        }
        return output;
    };

    /** Optimized startsWith function
     *  @param {String} source String to evaluate
     *  @param {String} start Pattern to look for
     *  @param {Boolean} ignoreCase If <code>true</code>, the function ignores the given strings' case
     *                              otherwise the match verification is exact. This is an optional
     *                              parameter with the default value <code>false</code>
     *  @return <code>true</code> if the <code>start</code> string starts the <code>source</code> string,
     *          <code>false</code> otherwise.
     *  @type Boolean
     *  @public
     *  @static
     */
    module.startsWith = function(source, start, ignoreCase) {
        if (source == null) {
            return false;
        }
        if (start == null) {
            return true;
        }
        var startLength = start.length;
        if (source.length < startLength) {
            return false;
        }
        var currentStart = source.substr(0, startLength);
        if (ignoreCase === true) {
            start = start.toLowerCase();
            currentStart = currentStart.toLowerCase();
        }
        return currentStart == start;
    };

})(); // End of the function limiting the scope of the private variables
