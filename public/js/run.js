(function($) {
    var bk = $("#go-back"),
    rlg = $("#behaviour .list-group"),
    rti = $("#behaviour h2 .fa"),
    cti = $("#config h2 .fa"),
    qs = $("#queries"), qsl = $(".list-group", qs),
    us = $("#updates"), usl = $(".list-group", us),
    pts = { 'string': "Text", 'float': "Number", 'date': "Date" },
    jct = { 
        'string': "String.class", 'float': "Float.class", 'date': "Date.class"
    },
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

    $("#behaviour h2 > a").click(function(){ 
        if (rti.hasClass("fa-caret-right")) {
            rti.removeClass("fa-caret-right").addClass("fa-caret-down")
        } else rti.removeClass("fa-caret-down").addClass("fa-caret-right");
    });

    $("#config h2 > a").click(function(){ 
        if (cti.hasClass("fa-caret-right")) {
            cti.removeClass("fa-caret-right").addClass("fa-caret-down")
        } else cti.removeClass("fa-caret-down").addClass("fa-caret-right");
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
    });

    var jc = $("#java .code").append('<div><span class="kn">import</span> <span class="nn">java.util.Arrays</span><span class="o">;</span></div><div><span class="kn">import</span> <span class="nn">java.util.Date</span><span class="o">;</span></div><div><span class="kn">import</span> <span class="nn">java.util.List</span><span class="o">;</span></div><div><span class="kn">import</span> <span class="nn">java.util.regex.Pattern</span><span class="o">;</span></div><div><span class="kn">import</span> <span class="nn">java.text.SimpleDateFormat</span><span class="o">;</span></div><div><span class="kn">import</span> <span class="nn">java.sql.Connection</span><span class="o">;</span></div><div><span class="kn">import</span> <span class="nn">java.sql.DriverManager</span><span class="o">;</span></div><div><span class="kn">import</span> <span class="nn">acolyte.CompositeHandler</span><span class="o">;</span></div><div><span class="kn">import</span> <span class="nn">acolyte.StatementHandler</span><span class="o">;</span></div><div><span class="kn">import</span> <span class="nn">acolyte.StatementHandler.Parameter</span><span class="o">;</span></div><div><span class="kn">import</span> <span class="nn">acolyte.ParameterMetaData</span><span class="o">;</span></div><div><span class="kn">import</span> <span class="nn">acolyte.QueryResult</span><span class="o">;</span></div><div><span class="kn">import</span> <span class="nn">acolyte.RowLists</span><span class="o">;</span></div><div><span class="kn">import</span> <span class="nn">acolyte.UpdateResult</span><span class="o">;</span></div><div><span class="kn">import</span> <span class="kd">static</span> <span class="nn">acolyte.RowList.Column</span><span class="o">;</span></div><div>&nbsp;</div><div><span class="kd">final</span> <span class="n">String</span> <span class="n">jdbcUrl</span> <span class="o">=</span> <span class="s">"jdbc:acolyte:anything-you-want?handler=my-unique-id"</span><span class="o">;</span></div><div>&nbsp;</div><div><span class="kd">final</span> <span class="n">SimpleDateFormat</span> <span class="n">dateFormat</span> <span class="o">=</span> <span class="k">new</span> <span class="n">SimpleDateFormat</span><span class="o">(</span><span class="s">"yyyy-MM-dd"</span><span class="o">,</span> <span class="n">java</span><span class="o">.</span><span class="na">util</span><span class="o">.</span><span class="na">Locale</span><span class="o">.</span><span class="na">ENGLISH</span><span class="o">);</span></div><div>&nbsp;</div><div><span class="n">StatementHandler</span> <span class="n">handler</span> <span class="o">=</span> <span class="k">new</span> <span class="n">CompositeHandler</span><span class="o">().</span></div>'),
    jqhs = $('<div>    <span class="n">withQueryHandler</span><span class="o">(</span><span class="k">new</span> <span class="n">CompositeHandler</span><span class="o">.</span><span class="na">QueryHandler</span><span class="o">()</span> <span class="o">{</span></div>').appendTo(jc);

    $('<div>            <span class="kd">public</span> <span class="n">QueryResult</span> <span class="nf">apply</span><span class="o">(</span><span class="n">String</span> <span class="n">sql</span><span class="o">,</span> <span class="n">List</span><span class="o">&lt;</span><span class="n">Parameter</span><span class="o">&gt;</span> <span class="n">parameters</span><span class="o">)</span> <span class="o">{</span></div>').appendTo(jc);
    var jqhe = $('<div>                <span class="k">throw</span> <span class="k">new</span> <span class="nf">RuntimeException</span><span class="o">(</span><span class="s">"Unsupported JDBC query"</span><span class="o">);</span></div>').appendTo(jc);
    jc.append('<div>            <span class="o">}</span></div><div>        <span class="o">}).</span></div><div>    <span class="n">withUpdateHandler</span><span class="o">(</span><span class="k">new</span> <span class="n">CompositeHandler</span><span class="o">.</span><span class="na">UpdateHandler</span><span class="o">()</span> <span class="o">{</span></div><div>            <span class="kd">public</span> <span class="n">UpdateResult</span> <span class="nf">apply</span><span class="o">(</span><span class="n">String</span> <span class="n">sql</span><span class="o">,</span> <span class="n">List</span><span class="o">&lt;</span><span class="n">Parameter</span><span class="o">&gt;</span> <span class="n">parameters</span><span class="o">)</span> <span class="o">{</span></div>');
    var juhe = $('<div>                <span class="k">throw</span> <span class="k">new</span> <span class="nf">RuntimeException</span><span class="o">(</span><span class="s">"Unsupported JDBC update"</span><span class="o">);</span></div>').appendTo(jc);
    $('<div>            <span class="o">}</span></div><div>        <span class="o">});</span></div><div>&nbsp;</div><div><span class="c1">// Register prepared handler with expected ID ' + "'my-unique-id'" + '</span></div><div><span class="n">acolyte</span><span class="o">.</span><span class="na">Driver</span><span class="o">.</span><span class="na">register</span><span class="o">(</span><span class="s">"my-unique-id"</span><span class="o">,</span> <span class="n">handler</span><span class="o">);</span></div><div>&nbsp;</div><div><span class="c1">// then when existing code do ...</span></div><div><span class="n">Connection</span> <span class="n">con</span> <span class="o">=</span> <span class="n">DriverManager</span><span class="o">.</span><span class="na">getConnection</span><span class="o">(</span><span class="n">jdbcUrl</span><span class="o">);</span></div>').appendTo(jc);

    $.each($._connection, function(i,r){
        var me, m = '<div>                <span class="k">if</span> <span class="o">(</span><span class="n">Pattern</span><span class="o">.</span><span class="na">matches</span><span class="o">(</span><span class="s">"' + r.pattern.expression.replace('"', '\\"') + '"</span><span class="o">,</span> <span class="n">sql</span><span class="o">)',
        ps = null;

        if (r.pattern['parameters'] && r.pattern.parameters.length > 0) {
            ps = '<div>                <span class="kd">final</span> <span class="n">List</span> <span class="n">ps' + i + '</span> <span class="o">=</span> <span class="n">Arrays</span><span class="o">.</span><span class="na">asList</span><span class="o">(</span>';

            $.each(r.pattern.parameters, function(j,p){
                if (j > 0) ps += ", ";

                ps += '<span class="n">Parameter</span><span class="o">.</span><span class="na">of</span><span class="o">(</span>';

                if (p._type == "float") {
                    ps += '<span class="n">ParameterMetaData</span><span class="o">.</span><span class="na">Float</span><span class="o">(</span><span class="mi">' + p.value + 'f</span><span class="o">),</span> <span class="mi">' + p.value + 'f</span><span class="o">)'

                } else if (p._type == "date") {
                    ps += '<span class="n">ParameterMetaData</span><span class="o">.</span><span class="na">Date</span><span class="o">(),</span> <span class="n">dateFormat</span><span class="o">.</span><span class="na">parse</span><span class="o">(</span><span class="s">"' + p.value + '"</span><span class="o">))'

                } else {
                    ps += '<span class="n">ParameterMetaData</span><span class="o">.</span><span class="na">Str</span><span class="o">,</span> <span class="s">"' + p.value.replace('"', '\\"') + '"</span><span class="o">)'

                }
            });
        }

        if (ps) {
            me = $(ps + ');</span></div>' + m + ' <span class="o">&amp;&amp;</span> <span class="n">ps' + i + '</span><span class="o">.</span><span class="na">equals</span><span class="o">(</span><span class="n">parameters</span><span class="o">))</span> <span class="o">{</span></div>')

        } else me = $(m + ') {</span></div>');

        if (r._type == "query") {
            $('<div>    <span class="n">withQueryDetection</span><span class="o">(</span><span class="s">"' + r.pattern.expression.replace('"', '\\"') + '"</span><span class="o">).</span></div>').insertBefore(jqhs);

            me.insertBefore(jqhe);

            if (r.result['error']) {
                $('<div>                    <span class="k">return</span> <span class="n">QueryResult</span><span class="o">.</span><span class="na">Nil</span><span class="o">.</span><span class="na">withWarning</span><span class="o">(</span><span class="s">"' + r.result.error.replace('"', '\\"') + '"</span><span class="o">);</span></div>').insertBefore(jqhe)

            } else {
                var cs = "", res = "";

                $.each(r.result.schema, function(j,c){
                    if (j > 0) cs += '<span class="o">,</span> ';

                    cs += '<span class="n">Column</span><span class="o">(</span><span class="n">' + jct[c._type] + '</span><span class="o">,</span> <span class="s">"' + c.name.replace('"', '\\"') + '"</span><span class="o">)</span>'

                });

                $.each(r.result.rows, function(j,d){
                    res += '<span class="o">.</span><span class="na">append</span><span class="o">(</span>';

                    $.each(d, function(k,cv){
                        var ct = r.result.schema[k]._type;

                        if (k > 0) res += '<span class="o">,</span> ';

                        if (ct == "float") {
                            res += '<span class="mi">' + cv + 'f</span>'
                        } else if (ct == "date") {
                            res += '<span class="n">dateFormat</span><span class="o">.</span><span class="na">parse</span><span class="o">(</span><span class="s">"'+cv+'"</span><span class="o">)</span>'

                        } else {
                            res += '<span class="s">"' + 
                                cv.replace('"', '\\"') + '"</span>'
                        }
                    });

                    res += '<span class="o">)</span>'
                });

                $('<div>                    <span class="k">return</span> <span class="n">RowLists</span><span class="o">.</span><span class="na">rowList' + r.result.schema.length + '</span><span class="o">(</span>' + cs + '<span class="o">)</span>' + res + '<span class="o">.</span><span class="na">asResult</span><span class="o">();</span></div>').insertBefore(jqhe)

            }

            $('<div>                <span class="o">}</span></div><div>&nbsp;</div>').insertBefore(jqhe)

        } else {
            me.insertBefore(juhe);

            if (r.result['error']) {
                $('<div>                    <span class="k">return</span> <span class="n">UpdateResult</span><span class="o">.</span><span class="na">Nothing</span><span class="o">.</span><span class="na">withWarning</span><span class="o">(</span><span class="s">"' + r.result.error.replace('"', '\\"') + '"</span><span class="o">);</span></div>').insertBefore(juhe)

            } else {
                $('<div>                    <span class="k">return</span> <span class="k">new</span> <span class="nf">UpdateResult</span><span class="o">(</span><span class="mi">' + r.result.updateCount + '</span><span class="o">);</span></div>').insertBefore(juhe)

            }

            $('<div>                <span class="o">}</span></div><div>&nbsp;</div>').insertBefore(juhe)
        }
    });

    var sc = $("#scala .code").append('<div><span class="k">import</span> <span class="nn">java.sql.</span><span class="o">{</span> <span class="nc">Connection</span> <span class="k">⇒</span> <span class="nc">SqlConnection</span><span class="o">,</span> <span class="nc">Date</span><span class="o">,</span> <span class="nc">DriverManager</span> <span class="o">}</span></div><div><span class="k">import</span> <span class="nn">acolyte.</span><span class="o">{</span> <span class="nc">Driver</span> <span class="k">⇒</span> <span class="nc">AcolyteDriver</span><span class="o">,</span> <span class="nc">QueryExecution</span><span class="o">,</span> <span class="nc">UpdateExecution</span> <span class="o">}</span></div><div><span class="k">import</span> <span class="nn">acolyte.RowLists.</span><span class="o">{</span> <span class="n">rowList1</span><span class="o">,</span> <span class="n">rowList2</span><span class="o">,</span> <span class="n">rowList3</span> <span class="o">}</span></div><div><span class="k">import</span> <span class="nn">acolyte.Acolyte</span> <span class="c1">// DSL</span></div><div><span class="k">import</span> <span class="nn">acolyte.Implicits._</span></div><div>&nbsp;</div><div><span class="c1">// Prepare handler</span></div><div><span class="k">val</span> <span class="n">handler</span><span class="k">:</span> <span class="kt">CompositeHandler</span> <span class="o">=</span> <span class="nc">Acolyte</span><span class="o">.</span><span class="n">handleStatement</span><span class="o">.</span></div><div>  <span class="n">withQueryDetection</span><span class="o">(</span></div>'),
    sqhs = $('<div><span class="o">).</span></div><div>  <span class="n">withQueryHandler</span> <span class="o">{</span> <span class="n">e</span><span class="k">:</span> <span class="kt">QueryExecution</span> <span class="k">⇒</span></div>').appendTo(sc),
    suhs = $('<div>  <span class="o">}</span> <span class="n">withUpdateHandler</span> <span class="o">{</span> <span class="n">e</span><span class="k">:</span> <span class="kt">UpdateExecution</span> <span class="k">⇒</span></div>').appendTo(sc);

    /*
    var qhs = $('<p>withQueryHandler(new CompositeHandler.QueryHandler() {</p><p>public QueryResult apply(String sql, List&lt;Parameter&gt; parameters) {</p>'),
    qhe = $('<p>throw new RuntimeException("Unsupported JDBC query");</p><p>}).</p>'), uhe = $('<p>throw new RuntimeException("Unsupported JDBC update");</p><p>}</p>');
    
    x = $("#java .code").
        append('<p>import java.util.Arrays;</p>').
        append('<p>import java.util.Date;</p>').
        append('<p>import java.util.List;</p>').
        append('<p>import java.util.regexp.Pattern;</p>').
        append('<p>import java.text.SimpleDateFormat;</p>').
        append('<p>import acolyte.CompositeHandler;</p>').
        append('<p>import acolyte.StatementHandler;</p>').
        append('<p>import acolyte.StatementHandler.Parameter;</p>').
        append('<p>import acolyte.ParameterMetaData;</p>').
        append('<p>import acolyte.QueryResult;</p>').
        append('<p>import acolyte.RowLists;</p>').
        append('<p>import acolyte.UpdateResult;</p>').
        append('<p>static import acolyte.RowList.Column;</p>').
        append('<p>SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH);</p>').
        append('<p>StatementHandler handler = new CompositeHandler().</p>').
        append(qhs).append(qhe).
        append('<p>withUpdateHandler(new CompositeHandler.UpdateHandler() {</p>').
        append('<p>public UpdateResult apply(String sql, List&lt;String&gt; parameters) {</p>').
        append(uhe).
        append('<p>});</p>');

    $.each($._connection, function(i,r){
        var me, m = 'Pattern.matches("' + 
            r.pattern.expression.replace('"', '\\"') + '", sql)',
        ps = null;

        if (r.pattern['parameters'] && r.pattern.parameters.length > 0) {
            ps = 'final List<Parameter> ps' + i + ' = Arrays.asList(';

            $.each(r.pattern.parameters, function(j,p){
                if (j > 0) ps += ", ";

                ps += 'Parameter.of(';

                if (p._type == "float") {
                    ps += 'ParameterMetaData.Float('+p.value+'), '+p.value+')'
                } else if (p._type == "date") {
                    ps += 'ParameterMetaData.Date(), dateFormat.parse("' 
                        + p.value + '"))'

                } else {
                    ps += 'ParameterMetaData.Str(), "' + 
                        p.value.replace('"', '\\"') + '")'

                }
            });

            ps += ');'
        }

        if (ps) {
            me = $('<p>' + ps + '</p><p>if (' + m + ' && ps' + i 
                   + '.equals(parameters)) {</p>')
        } else me = $('<p>if (' + m + ') {</p>');

        if (r._type == "query") {
            $('<p>withQueryDetection("' + 
              r.pattern.expression + '").</p>').insertBefore(qhs);

            me.insertBefore(qhe);

            if (r.result['error']) {
                $('<p>  return QueryResult.Nil.withWarning("' 
                 + r.result.error.replace('"', '\\"') + '");</p>').
                    insertBefore(qhe)
            } else {
                var cs = "", res = "";

                $.each(r.result.schema, function(j,c){
                    if (j > 0) cs += ", ";

                    cs += 'Column(' + jct[c._type] + ', "' + 
                        c.name.replace('"', '\\"') + '")';
                });

                $.each(r.result.rows, function(j,d){
                    res += '.append(';

                    $.each(d, function(k,cv){
                        var ct = r.result.schema[k]._type;

                        if (k > 0) res += ", ";

                        if (ct == "float") res += cv;
                        else if (ct == "date") {
                            res += 'dateFormat.parse("'+cv+'")';
                        } else res += '"' + cv.replace('"', '\\"') + '"'
                    });

                    res += ')'
                });

                $('<p>  return RowLists.rowList' + r.result.schema.length
                  + '(' + cs + ')' + res + '.asResult();</p>').
                    insertBefore(qhe)
            }

            $('<p>}</p>').insertBefore(qhe)
        } else {
            me.insertBefore(uhe);

            if (r.result['error']) {
                $('<p>  return UpdateResult.Nothing.withWarning("' 
                  + r.result.error.replace('"', '\\"') + '");</p>').
                    insertBefore(uhe)
            } else {
                $('<p> return new UpdateResult('+r.result.updateCount+');</p>').
                    insertBefore(uhe)
            }

            $('<p>}</p>').insertBefore(uhe)
        }
    })
    */
})(jQuery);
