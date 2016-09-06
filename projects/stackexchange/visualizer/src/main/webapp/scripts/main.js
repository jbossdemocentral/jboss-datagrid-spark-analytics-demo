var restBaseUrl= "http://" + document.location.hostname + ":" + document.location.port + "/" + document.location.pathname.split("/")[1] + "/rest/";


function drawDataGridDistribution() {
    var width = 960,
        height = 500,
        padding = 6, // separation between same-color nodes
        clusterPadding = 100, // separation between different-color nodes
        maxRadius = 25;

    var n = 2, // total number of nodes
        m = 10, // number of distinct clusters
        clusterIndex = 0;


    var color = d3.scale.category20()
        .domain(d3.range(m));

    // The largest node for each cluster.
    var clusters = new Array(m);

    var nodes = d3.range(m).map(function() {
          var i = clusterIndex++,
              r = maxRadius,
              d = {cluster: i, radius: r};
          if (!clusters[i]) clusters[i] = d;
          return d;
        });

//    var nodes = d3.range(n).map(function() {
//      var i = clusterIndex++,
//          r = Math.sqrt(20 / m * -Math.log(0.5)) * maxRadius,
//          d = {cluster: i, radius: r};
//      if (!clusters[i]) clusters[i] = d;
//      return d;
//    });

    // Use the pack layout to initialize node positions.
    d3.layout.pack()
        .sort(null)
        .size([width, height])
        .children(function(d) { return d.values; })
        .value(function(d) { return d.radius * d.radius; })
        .nodes({values: d3.nest()
          .key(function(d) { return d.cluster; })
          .entries(nodes)});

    var force = d3.layout.force()
        .nodes(nodes)
        .size([width, height])
        .gravity(.02)
        .charge(0)
        .on("tick", tick)
        .start();

    var svg = d3.select("#datagrid-nodes").append("svg")
        .attr("width", width)
        .attr("height", height);

    var node = svg.selectAll("circle")
        .data(nodes)
      .enter().append("circle")
        .style("fill", function(d) { return color(d.cluster); })
        .call(force.drag);

    node.transition()
        .duration(750)
        .delay(function(d, i) { return i * 5; })
        .attrTween("r", function(d) {
          var i = d3.interpolate(0, d.radius);
          return function(t) { return d.radius = i(t); };
        });

    function tick(e) {
      node
          .each(cluster(10 * e.alpha * e.alpha))
          .each(collide(.5))
          .attr("cx", function(d) { return d.x; })
          .attr("cy", function(d) { return d.y; });
    }

    // Move d to be adjacent to the cluster node.
    function cluster(alpha) {
      return function(d) {
        var cluster = clusters[d.cluster];
        if (cluster === d) return;
        var x = d.x - cluster.x,
            y = d.y - cluster.y,
            l = Math.sqrt(x * x + y * y),
            r = d.radius + cluster.radius;
        if (l != r) {
          l = (l - r) / l * alpha;
          d.x -= x *= l;
          d.y -= y *= l;
          cluster.x += x;
          cluster.y += y;
        }
      };
    }

    // Resolves collisions between d and all other circles.
    function collide(alpha) {
      var quadtree = d3.geom.quadtree(nodes);
      return function(d) {
        var r = d.radius + maxRadius + Math.max(padding, clusterPadding),
            nx1 = d.x - r,
            nx2 = d.x + r,
            ny1 = d.y - r,
            ny2 = d.y + r;
        quadtree.visit(function(quad, x1, y1, x2, y2) {
          if (quad.point && (quad.point !== d)) {
            var x = d.x - quad.point.x,
                y = d.y - quad.point.y,
                l = Math.sqrt(x * x + y * y),
                r = d.radius + quad.point.radius + (d.cluster === quad.point.cluster ? padding : clusterPadding);
            if (l < r) {
              l = (l - r) / l * alpha;
              d.x -= x *= l;
              d.y -= y *= l;
              quad.point.x += x;
              quad.point.y += y;
            }
          }
          return x1 > nx2 || x2 < nx1 || y1 > ny2 || y2 < ny1;
        });
      };
    }

}


function drawKeywordranking() {

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
//    d3.json("keyword-ranking-test.json", function(error, data) {
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
                .attr("height", function(d) { return height - y(d.value); });
    });

}


function drawLocationPieChart() {
    var margin = {top: 20, right: 20, bottom: 0, left: 20},
                width = $("#location-piechart").innerWidth() - margin.left - margin.right,
                height = $("#location-piechart").innerWidth()*0.7  - margin.top - margin.bottom;
        radius = Math.min(width, height) / 2;

//    var color = d3.scale.ordinal()
//        .range(["#98abc5", "#8a89a6", "#7b6888", "#6b486b", "#a05d56", "#d0743c", "#ff8c00"]);

    var arc = d3.svg.arc()
        .outerRadius(radius - 10)
        .innerRadius(0);

    var labelArc = d3.svg.arc()
        .outerRadius(radius - 40)
        .innerRadius(radius - 40);

    var pie = d3.layout.pie()
        .sort(null)
        .value(function(d) { return d.numOfPosts; });

//    var svg = d3.select("body").append("svg")
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
//    d3.json("keyword-ranking-test.json", function(error, data) {
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
                .attr("height", function(d) { return height - y(d.value); });
    });

}
//highest-ranked-users

drawDataGridDistribution()
drawKeywordranking();
drawLocationPieChart();
drawUserWithHighestRanking();