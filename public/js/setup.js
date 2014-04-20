(function($) {
    $('.has-tooltip').one('mouseover', rt).tooltip()

    var ar = $('a[href="#add-rule"]'),
    rt = function(){
        ar.tooltip('destroy').tooltip({
            'title': "Define a case by adding a rule", 'placement': "bottom"
        })
    }, 
    rl = $("#case-rules ol"),
    rd = $("#route-editor"), rf = $("#rule-form"), rsf = $("#result-form"),
    pc = $("#param-pattern .badge"), pp = $("#param-pattern .position"),
    pl = $("#param-pattern ol"),
    pt = $("#param-type"),
    rk = $("#ruleKind"), 
    re = $("#ruleExp").on('keyup change', function() {
        var ev = re.val();

        if (ev == "") {
            re.addClass("has-error"); 
            return adr.attr("disabled", "disabled")
        } 

        // ---

        try {
            new RegExp(ev)

            re.removeClass("has-error"); 
            adr.removeAttr("disabled")
        } catch (e) {
            re.addClass("has-error"); 
            adr.attr("disabled", "disabled")
        }
    }),
    arb = $("#route-editor .ac-apply"),
    rer = $("#result-error"), red = $("#result-data"),
    nxb = $("#route-editor .ac-next").click(function() {
        var e = $(this);

        if (rk.val() == "update") {
            var uc = $('<input type="number" min="0" disabled="disabled" id="update-count" class="form-control" />'),
            cf = function() {
                var re = $(this);

                if (re.is(":checked")) uc.removeAttr("disabled");
                else uc.attr("disabled", "disabled")
            };

            $('<div class="input-group"></div>').append(uc).prepend($('<label class="input-group-addon"> Update count</label>').prepend($('<input type="radio" name="result-type" id="update-success"/>').on('keyup change', cf))).tooltip({'title':"Execution is successful, then count of updated rows is returned."}).appendTo(red.empty())
 
        } else {
            var uc = $('<select disabled="disabled" id="row-size" class="form-control"><option value="1">1 column</option><option value="2">2 columns</option><option value="3">3 columns</option></select>').tooltip({'title':"How many columns there will be on each result row?",'trigger':"focus"}),
            rpl = $('<div></div>').append($('<label for="col1">Add row</label>').tooltip({'title':"Add a row to result set"})),
            nre = $('<div class="row"></div>').appendTo(rpl.appendTo(red.empty()).css({'display':"none"})),
            cf = function() {
                var re = $(this);

                if (re.is(":checked")) {
                    nre.empty();
                    rpl.css({'display':"block"});
                    uc.removeAttr("disabled")
                } else {
                    rpl.css({'display':"none"});
                    uc.attr("disabled", "disabled")
                }
            };

            $('<div class="input-group"></div>').append(uc).prepend($('<label class="input-group-addon"> Result set</label>').tooltip({'title':"Execution is successful, then some rows are returned."}).prepend($('<input type="radio" name="result-type" id="query-success"/>').change(cf))).prependTo(red.prepend('<p class="text-muted">Column number is limited on purpose for this tour.</p>'))
        }

        rf.fadeOut(500, function() { 
            rsf.fadeIn(500, function() { 
                e.css({'display':"none"});
                arb.css({'display':"inline"})
            })
        })
    }),
    pts = { 'string': "Text", 'float': "Number", 'date': "Date" },
    rmp = function() {
        var c = parseInt(pc.text());

        $(this).remove();

        if (c == 1) adr.attr("disabled", "disabled");

        pp.text(c); pc.text(c-1)
    },
    pb = $("#param-pattern .btn").click(function() {
        var n = parseInt(pp.text()), t = pt.val(), 
        v = $("#param-value").val(),
        i = $('<i class="fa fa-minus-circle"></i>');

        $('<li>' + pts[t] + ' = <tt class="text-info">' + v + '</tt></li>').prepend(i).
            data("type", t).data("value", v).appendTo(pl).
            hover(function() { i.css({'visibility':"visible"}) },
                  function() { i.css({'visibility':"hidden"}) }).
            click(rmp);

        adr.removeAttr("disabled");

        pp.text(n+1); pc.text(n);

        return false
    }),
    vf = function(f) {
        return function() {
            var e = $(this);
            if (!f(e)) { 
                e.addClass("has-error"); 
                pb.attr("disabled", "disabled")
            } else { e.removeClass("has-error"); pb.removeAttr("disabled") }
        }
    },
    pvs = vf(function(e) { return ($.trim(e.val()) == "") ? false : true }),
    pvi = vf(function(e) { return $.isNumeric(e.val()) }),
    pvt = {'title':"Expected value parameter",'trigger':"focus"},
    pvr = function() {
        var pv = $('<input type="text" class="form-control" id="param-value" />').tooltip(pvt).on('keyup change', pvs);
        
        $("#param-value").replaceWith(pv)
    };

    $.each(pts, function(v, t) {
        pt.append('<option value="'+v+'">'+t+'</option>')
    });

    ar.one('mouseover', rt).tooltip({
        'title': "First, add (at least) a rule to define which access case(s) you want to handle.",
        'placement': "bottom",
    }).on('hidden.bs.tooltip', rt).
        one('shown.bs.tooltip', function(){ setTimeout(rt, 15000) }).
        tooltip('show').
        click(function(){ 
            rf.trigger('reset').css({'display':"block"});
            rsf.trigger('reset').css({'display':"none"});
            nxb.css({'display':"inline"});
            arb.css({'display':"none"});
            red.empty();
            rd.modal();
            return false
        });

    rf.on('reset', pvr);

    pt.change(function() {
        var v = $(this).val();

        pb.attr("disabled", "disabled");

        if (v == "date") {
            $("#param-value").replaceWith($('<input type="text" class="form-control ac-date" id="param-value" readonly="readonly" />').tooltip(pvt).datepicker().one('changeDate', function() { pb.removeAttr("disabled") }))

        } else if (v == "float") {
            $("#param-value").replaceWith($('<input type="text" class="form-control" id="param-value" />').tooltip(pvt).on('keyup change', pvi))

        } else pvr();
    });

    $("#raise-error").change(function(){
        var e = $(this);
        if (e.is(":checked")) rer.removeAttr("disabled")
        else rer.attr("disabled", "disabled")
    });
})(jQuery);
