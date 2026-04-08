window.addEventListener("DOMContentLoaded", () => {
    view.getSeverityDistributionJson(function (data) {
        var json = data.responseJSON;
        generateSeverityDistributionBar("severity-distribution", "Severity Distribution",
            json.critical, json.high, json.medium, json.low, json.info, json.unassigned);
    });

    view.getFindingsJson(function (data) {
        (function ($) {
            var json = data.responseJSON;

            // Attach a shared renderer for columns that carry {display, sort} objects.
            // All other columns return the raw value as-is.
            var render = function(val, type) {
                if (val && val.display !== undefined) {
                    return (type === 'sort' || type === 'type') ? val.sort : val.display;
                }
                return val;
            };
            json.columns.forEach(function(col) { col.render = render; });

            $('#findings-table').DataTable({
                data: json.rows,
                columns: json.columns,
                responsive: true,
                order: []
            });
        })(jQuery);
    });
});
