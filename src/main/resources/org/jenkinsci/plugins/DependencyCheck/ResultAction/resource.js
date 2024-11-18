window.addEventListener("DOMContentLoaded", () => {
    view.getSeverityDistributionJson(function (data) {
        var json = data.responseJSON;
        generateSeverityDistributionBar("severity-distribution", "Severity Distribution",
            json.critical, json.high, json.medium, json.low, json.info, json.unassigned);
    });

    view.getFindingsJson(function (data) {
        (function ($) {
            $('.table').footable(data.responseJSON);
        })(jQuery);
    });
});
