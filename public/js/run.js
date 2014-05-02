(function($) {
    var bk = $("#go-back"),
    rlg = $("#connection .list-group"),
    rti = $("#connection h2 .fa"),
    qs = $("#queries"), qsl = $(".list-group", qs),
    us = $("#updates"), usl = $(".list-group", us),
    pts = { 'string': "Text", 'float': "Number", 'date': "Date" },
    exb = $("#execute .btn"),
    stg = $("#execute .form-group"),
    stmt = $("#statement"),
    res = $("#result"),
    resp = $("#result .panel-collapse");

    $('.has-tooltip').tooltip();

    $("a", bk).click(function(){ 
        bk.trigger('submit'); 
        return false 
    });

    $("#connection h2 > a").click(function(){ 
        if (rti.hasClass("fa-caret-right")) {
            rti.removeClass("fa-caret-right").addClass("fa-caret-down")
        } else rti.removeClass("fa-caret-down").addClass("fa-caret-right");
    });

    $.each($._connection, function(i,r){
        var pat = $('<p>Pattern = <tt class="text-info">' + 
                    r.pattern.expression + '</tt></p>'),
        li = $('<li class="list-group-item"></li>').append(pat),
        pps = null;

        if (r.pattern['parameters'] && r.pattern.parameters['length'] &&
            r.pattern.parameters.length > 0) {
            var t = $('<table class="table table-striped"><thead><tr><th>Type</th><th>Value</th></thead></table>'),
            tb = $('<tbody></tbody>').appendTo(t);
            
            pps = $('<div>Parameter constraints</div>').append(t);

            $.each(r.pattern.parameters, function(j,p){
                $('<tr><td>'+p._type+'</td><td>'+p.value+'</td></tr>').
                    appendTo(tb)
            })
        } else pps = $('<em>No parameter constraint</em>');

        if (r.result['error']) {
            $('<p class="simple-result">Error = <tt class="text-info">' + r.result.error + '</tt></p>').insertAfter(pat.prepend('<i class="fa fa-angle-right"></i>').css({'display':"inline"}))
        } else if (r._type == "update") {
            $('<p class="simple-result">Update count = <tt class="text-info">' + r.result['updateCount'] + '</tt></p>').insertAfter(pat.prepend('<i class="fa fa-angle-right"></i>').css({'display':"inline"}))
            
        } else {
            var cl = $('<div></div>'),
            t = $('<table class="table table-striped table-bordered table-responsive"></table>').appendTo(cl), 
            th = $('<tr></tr>').appendTo($('<thead></thead>').appendTo(t)),
            tb = $('<tbody></tbody>').appendTo(t),
            pati = $('<i class="fa fa-caret-right"></i>').prependTo(pat);

            $.each(r.result['schema'], function(j,c){
                $('<th>' + c.name + ' (' + pts[c._type] + ')</th>').appendTo(th)
            });

            $.each(r.result['rows'], function(j,row){
                var re = $('<tr></tr>'), 
                k;

                for (k = 0; k < row.length; k++) {
                    $('<td>'+row[k]+'</td>').appendTo(re)
                }

                tb.append(re)
            });

            li.append(cl.collapse('toggle'));
            
            pat.css({'cursor':"pointer"}).click(function(){ 
                if (pati.hasClass("fa-caret-right")) {
                    pati.removeClass("fa-caret-right").addClass("fa-caret-down")
                } else {
                    pati.removeClass("fa-caret-down").addClass("fa-caret-right")
                }

                cl.collapse('toggle') 
            })
        }

        li.popover({
            'placement': "top", 'trigger': "hover", 'html': true, 'content': pps
        });

        if (r._type == "update") usl.append(li);
        else qsl.append(li)
    });

    if ($(".list-group-item", qsl).length > 0) qs.css({'display':"block"});

    if ($(".list-group-item", usl).length > 0) us.css({'display':"block"});

    stmt.on('change keyup', function(){
        var v = $.trim(stmt.val());

        if (v == "") {
            stg.addClass("has-warning");
            exb.attr("disabled", "disabled")
        } else {
            stg.removeClass("has-warning");
            exb.removeAttr("disabled")
        }
    });

    exb.click(function(){ 
        res.removeClass("panel-default").addClass("panel-info");
        resp.collapse('toggle');

        $(".panel-body", res).append('<p class="text-muted">No result</p>');

        return false
    })
})(jQuery);
