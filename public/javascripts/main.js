$(function(){
    $('#select-all').click(function(event) {
        if(this.checked) {
            // Iterate each checkbox
            $('.schedule:checkbox').each(function() {
                this.checked = true;
            });
        }
        else {
            $(':checkbox').each(function() {
                this.checked = false;
            });
        }
    });
});

$(function(){
    $('.input-daterange').datepicker({
        format: 'dd-mm-yyyy',
        todayBtn: true,
        todayHighlight: true
    });
});

$(function(defaultDateStart, defaultDateEnd){
    $('.daterange').change(function(){
        var dateStart = document.getElementById("dateStart").value;
        var dateEnd = document.getElementById("dateEnd").value;

        if(dateStart !== defaultDateStart || dateEnd !== defaultDateEnd) {
            $('#filterDateRange').submit();
        }
    });
});

$(function(selectedMonth){
    $('#monthSelection').change(function(){
        var dateStart = document.getElementById("month").value;

        if(dateStart !== selectedMonth) {
            $('#monthNumber').submit();
        }
    });
});

$(document).ready(function() {
    allSelected();

    $('button.js-delete-confirmation').click(function() {
        return confirm("Are you sure you want to delete?");
    })
});

$(".schedule").change(function(){
    allSelected();
});

function allSelected(){
    var allSelected = $('.schedule:checked').length === $('.schedule').length;

    if(allSelected) {
        $('#select-all').each(function() {
            this.checked = true;
        });
    }
}

$(function() {
    $('#formAttendance').submit(function (eventObj) {
        $('.schedule:checked').each(function () {
            $(this).append('<input type="hidden" name="menuDate[]" value="' + this.parentNode.parentNode.cells.item(1).textContent.toString() + '" />');
        });
        return true;
    });
});

$(function(){
    // TableExport(document.getElementsByTagName("table"))//, {
    //     headers: true,                              // (Boolean), display table headers (th or td elements) in the <thead>, (default: true)
    //     footers: true,                              // (Boolean), display table footers (th or td elements) in the <tfoot>, (default: false)
    //     formats: ['xls', 'csv', 'txt'],             // (String[]), filetype(s) for the export, (default: ['xls', 'csv', 'txt'])
    //     filename: 'id',                             // (id, String), filename for the downloaded file, (default: 'id')
    //     bootstrap: false,                           // (Boolean), style buttons using bootstrap, (default: true)
    //     exportButtons: true,                        // (Boolean), automatically generate the built-in export buttons for each of the specified formats (default: true)
    //     position: 'bottom',                         // (top, bottom), position of the caption element relative to table, (default: 'bottom')
    //     ignoreRows: null,                           // (Number, Number[]), row indices to exclude from the exported file(s) (default: null)
    //     ignoreCols: null,                           // (Number, Number[]), column indices to exclude from the exported file(s) (default: null)
    //     trimWhitespace: true                        // (Boolean), remove all leading/trailing newlines, spaces, and tabs from cell text in the exported file(s) (default: false)
    // });
    $('.printTable').tableExport({
        formats: ['csv'],
        headers: true,
        footers: true
    });
});


