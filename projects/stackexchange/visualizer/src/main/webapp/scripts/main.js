var restBaseUrl= "http://" + document.location.hostname + ":" + document.location.port + "/" + document.location.pathname.split("/")[1] + "/rest/";
//var restBaseUrl= "http://localhost:8080/visualizer/rest/";
//var restBaseUrl2= "http://localhost:8080/jdg-visualizer/rest/";

var randomColor = (function(){
  var golden_ratio_conjugate = 0.618033988749895;
  var h = Math.random();

  var hslToRgb = function (h, s, l){
      var r, g, b;

      if(s == 0){
          r = g = b = l; // achromatic
      }else{
          function hue2rgb(p, q, t){
              if(t < 0) t += 1;
              if(t > 1) t -= 1;
              if(t < 1/6) return p + (q - p) * 6 * t;
              if(t < 1/2) return q;
              if(t < 2/3) return p + (q - p) * (2/3 - t) * 6;
              return p;
          }

          var q = l < 0.5 ? l * (1 + s) : l + s - l * s;
          var p = 2 * l - q;
          r = hue2rgb(p, q, h + 1/3);
          g = hue2rgb(p, q, h);
          b = hue2rgb(p, q, h - 1/3);
      }

      return '#'+Math.round(r * 255).toString(16)+Math.round(g * 255).toString(16)+Math.round(b * 255).toString(16);
  };
  
  return function(){
    h += golden_ratio_conjugate;
    h %= 1;
    return hslToRgb(h, 0.5, 0.60);
  };
})();

function drawKeywordRanking() {

    var margin = {top: 20, right: 20, bottom: 30, left: 40},
            width = $("#keyword-ranking").innerWidth() - margin.left - margin.right,
            height = $("#keyword-ranking").innerWidth()*0.7  - margin.top - margin.bottom;

    var x = d3.scale.ordinal()
            .rangeRoundBands([0, width], .1);

    var y = d3.scale.linear()
            .range([height, 0]);

    var xAxis = d3.svg.axis()
            .scale(x)
            .orient("bottom");

    var yAxis = d3.svg.axis()
            .scale(y)
            .orient("left");

    var svg = d3.select("#keyword-ranking").append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    d3.json(restBaseUrl + "results/keywordcount", function(error, data) {
        if (error) throw error;

        x.domain(d3.keys(data));
        y.domain([0, d3.max(d3.values(data))]);

        svg.append("g")
                .attr("class", "x axis")
                .attr("transform", "translate(0," + height + ")")
                .call(xAxis);

        svg.append("g")
                .attr("class", "y axis")
                .call(yAxis);

        svg.selectAll(".bar")
                .data(d3.entries(data))
                .enter().append("rect")
                .attr("class", "bar")
                .attr("x", function(d) { return x(d.key); })
                .attr("width", x.rangeBand())
                .attr("y", function(d) { return y(d.value); })
                .attr("height", function(d) { return height - y(d.value); })
                .style({fill: randomColor});
    });

}


function drawLocationPieChart() {

    var margin = {top: 20, right: 20, bottom: 0, left: 20},
                width = $("#location-piechart").innerWidth() - margin.left - margin.right,
                height = $("#location-piechart").innerWidth()*0.7  - margin.top - margin.bottom;
        radius = Math.min(width, height) / 2;

    var arc = d3.svg.arc()
        .outerRadius(radius - 10)
        .innerRadius(0);

    var labelArc = d3.svg.arc()
        .outerRadius(radius - 40)
        .innerRadius(radius - 40);

    var pie = d3.layout.pie()
        .sort(null)
        .value(function(d) { return d.numOfPosts; });

    var svg = d3.select("#location-piechart").append("svg")
        .attr("width", width)
        .attr("height", height)
        .append("g")
        .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")");

    d3.json(restBaseUrl + "results/locations", function(error, data) {
        if (error) throw error;
        console.log(JSON.stringify(data));

        var color = d3.scale.category20b()
                .domain(d3.values(data.data,  function(d) { return d.location; } ));

        var g = svg.selectAll(".arc")
            .data(pie(data))
          .enter().append("g")
            .attr("class", "arc");

        g.append("path")
            .attr("d", arc)
            .style("fill", function(d) { return color(d.data.location); });

        g.append("text")
            .attr("transform", function(d) { return "translate(" + labelArc.centroid(d) + ")"; })
            .attr("dy", ".35em")
            .text(function(d) { return d.data.location; });
     });


}


function drawUserWithHighestRanking() {
    var margin = {top: 20, right: 20, bottom: 30, left: 40},
            width = $("#highest-ranked-users").innerWidth() - margin.left - margin.right,
            height = $("#highest-ranked-users").innerWidth()*0.7  - margin.top - margin.bottom;

    var x = d3.scale.ordinal()
            .rangeRoundBands([0, width], .1);

    var y = d3.scale.linear()
            .range([height, 0]);

    var xAxis = d3.svg.axis()
            .scale(x)
            .orient("bottom");

    var yAxis = d3.svg.axis()
            .scale(y)
            .orient("left");


    var svg = d3.select("#highest-ranked-users").append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    d3.json(restBaseUrl + "results/userranking", function(error, data) {
        if (error) throw error;


        console.log("keys: " + d3.keys(data));
        console.log("values: " + d3.values(data));
        console.log("max: " + d3.max(d3.values(data)));
        console.log("entries: " + d3.entries(data));


        x.domain(d3.keys(data));
        y.domain([0, d3.max(d3.values(data))]);

        svg.append("g")
                .attr("class", "x axis")
                .attr("transform", "translate(0," + height + ")")
                .call(xAxis);

        svg.append("g")
                .attr("class", "y axis")
                .call(yAxis);
//                .append("text")
//                .attr("transform", "rotate(-90)")
//                .attr("y", 6)
//                .attr("dy", ".71em")
//                .style("text-anchor", "end")
//                .text("Frequency");

        svg.selectAll(".bar")
                .data(d3.entries(data))
                .enter().append("rect")
                .attr("class", "bar")
                .attr("x", function(d) { return x(d.key); })
                .attr("width", x.rangeBand())
                .attr("y", function(d) { return y(d.value); })
                .attr("height", function(d) { return height - y(d.value); })
                .style({fill: randomColor});
    });

}
//highest-ranked-users



//drawDataGridDistribution();
//drawKeywordranking();


function startPollingKeywordRanking() {
    if(d3.selectAll("#keyword-ranking > svg")!=null) {
        d3.selectAll("#keyword-ranking > svg").remove();
    }
    drawKeywordRanking();
    setTimeout(function(){ startPollingKeywordRanking(); },3000);
}

function startPollingLocations() {
    if(d3.selectAll("#location-piechart > svg")!=null) {
        d3.selectAll("#location-piechart > svg").remove();
    }
    drawLocationPieChart();
    setTimeout(function(){ startPollingLocations(); },3000);
}


function startPollingUserWithHighestRanking() {
    if(d3.select("#highest-ranked-users > svg")!=null) {
        d3.select("#highest-ranked-users > svg").remove();
    }
    drawUserWithHighestRanking();
    setTimeout(function(){ startPollingUserWithHighestRanking(); },3000);
}


$(document).ready(function() {
    startPollingKeywordRanking();
    startPollingLocations();
    startPollingUserWithHighestRanking();
});

