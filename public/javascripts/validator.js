(function (plugin) {
    /* istanbul ignore next: differing implementations */
    if (typeof module !== 'undefined' && module !== null && module.exports) {
        module.exports = plugin
    } else if (typeof define === 'function' && define.amd) {
        define(['mithril'], plugin)
    } else if (typeof window !== 'undefined') {
        plugin(m)
    }
})(function MithrilValidator (m) {
    if (m.validator) {
        return m
    }

    /**
     * Validates mithril models and objects through validation functions
     * mapped to specific model properties.
     *
     * Example
     *
     *     Validator({
   *       name: function (name) {
   *         if (!name) {
   *           return "Name is required."
   *         }
   *       }
   *     }).validate({})
     *
     * @param  {Object} validations Map consisting of model properties to validation functions
     *
     * @return {Validator}
     */
    function Validator (validations) {
        this.errors = {}
        this.validations = validations
    }

    /**
     * Returns length of error map
     *
     * @return {Number}
     */
    Validator.prototype.hasErrors = function () {
        return Object.keys(this.errors).length
    }

    /**
     * Returns the element associated with the specified key
     *
     * @param  {String}  key
     * @return {Boolean}
     */
    Validator.prototype.hasError = function (key) {
        return this.errors[key]
    }

    /**
     * Removes all of the elements from the error list
     */
    Validator.prototype.clearErrors = function () {
        this.errors = {}
    }

    /**
     * Validates the specified model against the validations mapping in this instance.
     *
     * Each (shallow) property is iterated over and cross-checked against the given model for value,
     * then the validation function is invoked passing the model as context and value as the first argument.
     *
     * On a truthy result from a validation function the result is placed on the error object with the
     * property name as the identifier.
     *
     * @param  {Object} model       Key-value map of property to `m.prop` values
     *
     * @return {Validator}
     */
    Validator.prototype.validate = function (model) {
        var self = this

        this.clearErrors()

        Object.keys(this.validations).forEach(function (key, index) {
            validator = self.validations[key]
            value = model[key] ? (typeof model[key] === 'function' ? model[key]() : model[key]) : undefined
            result = validator.bind(model)(value)

            if (result) {
                self.errors[key] = result
            }
        })

        return this
    }

    // Export
    m.validator = Validator

    // Return patched mithril
    return m
})