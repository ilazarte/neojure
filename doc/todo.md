# Introduction to cljs-start-learn

TODO: write [great documentation](http://jacobian.org/writing/great-documentation/what-to-write/)

todo:
- how to configure the chart itself via web page
- bootstrap proj?

done:
- multiline chart with value bar
- add update function reference to atom? how is it configured?
- save source to github

Random:

	chart update pattern in clj:
	keep all the "datum" in an atom.
	
	
	=> (def v1 [{:area   true
         :values (range 1 10)
         :key    "Sine Wave"
         :color  "#ff7f0e"}
        {:values (range 10 20)
         :key    "Cosine Wave"
         :color  "#2ca02c"}
        {:values (range 30 40)
         :key    "Random Points"
         :color  "#2222ff"}
        {:values (range 40 50)
         :key    "Random Cosine"
         :color  "#667711"}])
	#'parkway.core/v1
	=> v1
	[{:area true, :values (1 2 3 4 5 6 7 8 9), :key "Sine Wave", :color "#ff7f0e"} {:values (10 11 12 13 14 15 16 17 18 19), :key "Cosine Wave", :color "#2ca02c"} {:values (30 31 32 33 34 35 36 37 38 39), :key "Random Points", :color "#2222ff"} {:values (40 41 42 43 44 45 46 47 48 49), :key "Random Cosine", :color "#667711"}]
	=> (update-in v1 [0 :values] #(conj % 10))
	[{:area true, :values (10 1 2 3 4 5 6 7 8 9), :key "Sine Wave", :color "#ff7f0e"} {:values (10 11 12 13 14 15 16 17 18 19), :key "Cosine Wave", :color "#2ca02c"} {:values (30 31 32 33 34 35 36 37 38 39), :key "Random Points", :color "#2222ff"} {:values (40 41 42 43 44 45 46 47 48 49), :key "Random Cosine", :color "#667711"}]
