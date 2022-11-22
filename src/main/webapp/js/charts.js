/**
 * Generates a responsive progress bar consisting of all severities.
 *
 * @param targetId the ID of the element to insert the bar in
 * @param label the label to give the bar
 * @param critical the number of critical severity vulnerabilities
 * @param high the number of high severity vulnerabilities
 * @param medium the number of medium severity vulnerabilities
 * @param low the number of low severity vulnerabilities
 * @param info the number of info-level issues
 * @param unassigned the number of issues with unassigned severity
 */
function generateSeverityDistributionBar(targetId, label, critical, high, medium, low, info, unassigned) {
    var percentCritical = (critical > 0 ? (critical/(critical+high+medium+low+info+unassigned))*100 : 0);
    var percentHigh = (high > 0 ? (high/(critical+high+medium+low+info+unassigned))*100 : 0);
    var percentMedium = (medium > 0 ? (medium/(critical+high+medium+low+info+unassigned))*100 : 0);
    var percentLow = (low > 0 ? (low/(critical+high+medium+low+info+unassigned))*100 : 0);
    var percentInfo = (info > 0 ? (info/(critical+high+medium+low+info+unassigned))*100 : 0);
    var percentUnassigned = (unassigned > 0 ? (unassigned/(critical+high+medium+low+info+unassigned))*100 : 0);
    var block = '<span class="odc-section-label ">' + label + '</span><div class="severity-distribution">';
    if (critical === 0 && high === 0 && medium === 0 && low === 0 && info === 0 && unassigned === 0) {
        block += '<div class="severity-distribution-bar severity-info-bg odc-tooltip" title="No Vulnerabilities Found" style="width:100%">No Vulnerabilities Found</div>';
    } else {
        block += '<div class="severity-distribution-bar severity-critical-bg odc-tooltip" title="Critical: ' + critical + ' (' + Math.round(percentCritical*10)/10 + '%)" style="width:' + percentCritical+ '%">' + critical + '</div>';
        block += '<div class="severity-distribution-bar severity-high-bg odc-tooltip" title="High: ' + high + ' (' + Math.round(percentHigh*10)/10 + '%)" style="width:' + percentHigh + '%">' + high + '</div>';
        block += '<div class="severity-distribution-bar severity-medium-bg odc-tooltip" title="Medium: ' + medium + ' (' + Math.round(percentMedium*10)/10 + '%)" style="width:' + percentMedium + '%">' + medium + '</div>';
        block += '<div class="severity-distribution-bar severity-low-bg odc-tooltip" title="Low: ' + low + ' (' + Math.round(percentLow*10)/10 + '%)" style="width:' + percentLow + '%">' + low + '</div>';
        block += '<div class="severity-distribution-bar severity-info-bg odc-tooltip" title="Info: ' + info + ' (' + Math.round(percentInfo*10)/10 + '%)" style="width:' + percentInfo + '%">' + info + '</div>';
        block += '<div class="severity-distribution-bar severity-unassigned-bg odc-tooltip" title="Unassigned: ' + unassigned + ' (' + Math.round(percentUnassigned*10)/10 + '%)" style="width:' + percentUnassigned + '%">' + unassigned + '</div>';
    }
    block += '</div>';
    document.getElementById(targetId).innerHTML = block;
    Tipped.create('.odc-tooltip');
}
