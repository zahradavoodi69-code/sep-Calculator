package com.sepehr.sepcalculator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.*

class MainActivity : AppCompatActivity() {
    private lateinit var display: android.widget.TextView
    private lateinit var historyView: android.widget.TextView
    private lateinit var title: android.widget.TextView
    private var expr = ""
    private var dark = true
    private val history = mutableListOf<String>()
    override fun onCreate(state: Bundle?) { super.onCreate(state); setContentView(R.layout.activity_main)
        display=findViewById(R.id.display); historyView=findViewById(R.id.history); title=findViewById(R.id.title)
        val nums=mapOf(R.id.n0 to "0",R.id.n1 to "1",R.id.n2 to "2",R.id.n3 to "3",R.id.n4 to "4",R.id.n5 to "5",R.id.n6 to "6",R.id.n7 to "7",R.id.n8 to "8",R.id.n9 to "9",R.id.dot to ".")
        nums.forEach{(id,v)->findViewById<Button>(id).setOnClickListener{add(v)}}
        mapOf(R.id.plus to "+",R.id.minus to "-",R.id.mul to "*",R.id.div to "/").forEach{(id,v)->findViewById<Button>(id).setOnClickListener{add(v)}}
        findViewById<Button>(R.id.paren).setOnClickListener{add(if(expr.count{it=='('}>expr.count{it==')'}) ")" else "(")}
        findViewById<Button>(R.id.percent).setOnClickListener{add("%")}
        findViewById<Button>(R.id.clear).setOnClickListener{expr="";update()}
        findViewById<Button>(R.id.back).setOnClickListener{if(expr.isNotEmpty())expr=expr.dropLast(1);update()}
        findViewById<Button>(R.id.equal).setOnClickListener{calculate()}
        findViewById<Button>(R.id.sin).setOnClickListener{unary("sin")};findViewById<Button>(R.id.cos).setOnClickListener{unary("cos")};findViewById<Button>(R.id.tan).setOnClickListener{unary("tan")};findViewById<Button>(R.id.sqrt).setOnClickListener{unary("sqrt")};findViewById<Button>(R.id.pow).setOnClickListener{unary("pow")}
        findViewById<Button>(R.id.copy).setOnClickListener{val cm=getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager;cm.setPrimaryClip(ClipData.newPlainText("sep Calculator",display.text));Toast.makeText(this,"Result copied",Toast.LENGTH_SHORT).show()}
        findViewById<Button>(R.id.historyButton).setOnClickListener{Toast.makeText(this,if(history.isEmpty())"No history yet" else history.takeLast(5).joinToString(" | "),Toast.LENGTH_LONG).show()}
        findViewById<Button>(R.id.themeButton).setOnClickListener{dark=!dark;theme()};theme();update()
    }
    private fun add(s:String){expr+=s;update()};private fun update(){display.text=if(expr.isEmpty())"0" else expr.replace("*","×").replace("/","÷")}
    private fun unary(fn:String){try{val x=Parser(expr).parse();val r=when(fn){"sin"->sin(Math.toRadians(x));"cos"->cos(Math.toRadians(x));"tan"->tan(Math.toRadians(x));"sqrt"->sqrt(x);else->x.pow(2)};expr=format(r);update()}catch(_:Exception){err()}}
    private fun calculate(){try{val o=expr;expr=format(Parser(expr).parse());history.add("$o = $expr");historyView.text="$o = $expr";update()}catch(_:Exception){err()}}
    private fun format(x:Double)=if(!x.isFinite())throw ArithmeticException() else if(abs(x-round(x))<1e-10)round(x).toLong().toString() else "%.10f".format(x).trimEnd('0').trimEnd('.')
    private fun err(){display.text="Error";expr="";Toast.makeText(this,"Invalid expression",Toast.LENGTH_SHORT).show()}
    private fun theme(){val bg=if(dark)Color.rgb(8,8,8)else Color.rgb(245,245,245);val fg=if(dark)Color.WHITE else Color.BLACK;findViewById<android.view.View>(R.id.grid).setBackgroundColor(bg);display.setTextColor(fg);title.setTextColor(fg);historyView.setTextColor(if(dark)Color.LTGRAY else Color.DKGRAY)}
    class Parser(private val s:String){var p=0;fun parse():Double{val v=exp();skip();if(p!=s.length)throw Exception();return v};fun exp():Double{var v=term();while(true){skip();v=when{take('+')->v+term();take('-')->v-term();else->return v}}};fun term():Double{var v=factor();while(true){skip();v=when{take('*')->v*factor();take('/')->{val d=factor();if(d==0.0)throw ArithmeticException();v/d};else->return v}}};fun factor():Double{skip();if(take('+'))return factor();if(take('-'))return-factor();if(take('(')){val v=exp();if(!take(')'))throw Exception();return pct(v)};val st=p;while(p<s.length&&(s[p].isDigit()||s[p]=='.'))p++;if(st==p)throw Exception();return pct(s.substring(st,p).toDouble())};fun pct(v:Double):Double{skip();return if(take('%'))v/100 else v};fun skip(){while(p<s.length&&s[p].isWhitespace())p++};fun take(c:Char)=run{skip();if(p<s.length&&s[p]==c){p++;true}else false}}
}
