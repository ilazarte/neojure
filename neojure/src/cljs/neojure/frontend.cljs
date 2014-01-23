;;; If this namespace requires macros, remember that ClojureScript's
;;; macros are written in Clojure and have to be referenced via the
;;; :require-macros directive where the :as keyword is required. Even
;;; if you can add files containing macros and compile-time only
;;; functions in the :source-paths setting of the :builds, it is
;;; strongly suggested to add them to the leiningen :source-paths.

;; Realtime chart example found in nvd3 github test/realTimeChartTest.html
;; 

(ns neojure.frontend)

(defn- index-vec
  "Find the index of the item by predicate"
  [coll pred]
  (first (keep-indexed #(when (pred %2) %1) coll)))

(defn- get-vec
  "Get the value from a collection by predicate"
  [coll pred]
  (first (filter pred coll)))

(defn- put-vec
  "Update a val by predicate"
  [coll pred val]
  (let [exists (some pred coll)
        updater #(if (pred %) val %)]
    (if exists
      (map updater coll)
      (conj coll val))))

(defn- push-vec
  [coll elem]
  (-> coll rest vec (conj elem)))

(defn- line-chart-model 
  []
  (let [opts {:margin             {:left 50 :bottom: 50}
              :x                  (fn [d i] i)
              :showXAxis          true
              :showYAxis          true
              :transitionDuration 250}] 
    (-> 
      (js/nv.models.lineChart)
      (.useInteractiveGuideline true)
      (.options (clj->js opts)))))

(defn- sin
  [val]
  (js/Math.sin val))

(defn- cos
  [val]
  (js/Math.cos val))

(defn- random
  []
  (js/Math.random))

;--------------------------------------------------------------------
; begin: state
;
;     the data atom will hold all charts.
;     the first level key will be a key to the id on the page
;     the second keys are the selector, chart instance, and data
;--------------------------------------------------------------------

(def data (atom {}))

;--------------------------------------------------------------------
; end: state
;--------------------------------------------------------------------

(defn sin-and-cos
  "Produce the sin and cos data used by the line chart nvd3 demo"
  [max]
  (let [r          (range 0 max)
   sin-fn     (fn [i] {:x i :y (if (= (mod i 10) 5) nil (sin (/ i 10)))})
   cos-fn     (fn [i] {:x i :y (* .5 (cos (/ i 10)))})
   rand-fn    (fn [i] {:x i :y (/ (random) 10)})
   rand2-fn   (fn [i] {:x i :y (+ (cos (/ i 10)) (/ (random) 10))})
   sin-coll   (mapv sin-fn r)
   cos-coll   (mapv cos-fn r)
   rand-coll  (mapv rand-fn r)
   rand2-coll (mapv rand2-fn r)]
    [{:area   true
      :values sin-coll
      :key    "Sine Wave"
      :color  "#ff7f0e"}
     {:values cos-coll
      :key    "Cosine Wave"
      :color  "#2ca02c"}
     {:values rand-coll
      :key    "Random Points"
      :color  "#2222ff"}
     {:values rand2-coll
      :key    "Random Cosine"
      :color  "#667711"}]))

(defn select 
  ([selector]
    (js/d3.select selector))
  ([selection selector]
    (.select selection selector)))

(defn select-all [selector]
  (js/d3.selectAll selector))

(defn append [selection tagname]
  (.append selection tagname))

(defn attr [selection key val]
  (.attr selection key val))

(defn style [selection val]
  (attr selection "style" val))

(defn size [selection]
  (.size selection))

(defn put-by-tag
  "Add an element if it does not exist by tag already to a parent selection"
  [selection tagname]
  (let [el  (select selection tagname)
        len (size el)]
    (if (= len 1)
      el
      (-> selection (append tagname)))))

(defn put-by-id 
  "Add an element if it does not exist by id already to a parent selection"
  [selection tagname id]
  (let [el  (select selection (str "#" id))
        len (size el)]
    (if (= len 1)
      el
      (-> selection (append tagname) (attr "id" id)))))

(defn line-chart
  "Create a line chart at the selector"
  [selector data config]
  (let [chart (line-chart-model)]
    (-> 
      (.-xAxis chart)
      (.axisLabel (:xlabel config))
      (.tickFormat (js/d3.format (:xformat config))))
    
    (-> 
      (.-yAxis chart)
      (.axisLabel (:ylabel config))
      (.tickFormat (js/d3.format (:yformat config))))
    
    (->
      (select selector)
      (.datum (clj->js data))
      (.transition)
      (.duration 500)
      (.call chart))

    (js/nv.utils.windowResize #(.update chart))
    
    chart))

(defn add-graph [f]
  (js/nv.addGraph f))

(defn put-chart-state!
  [idkey selector chart datum]
  (let [set-chart #(identity {:selector selector
                              :chart    chart
                              :datum    datum})]
    (swap! data #(update-in % [idkey] set-chart))
    
    nil))

(defn put-line-chart
  "Create a line chart at the id and update the data atom"
  [id]

  (let [bodyselection (select "body")
        idselection   (put-by-id bodyselection "div" id)
        svgselection  (put-by-tag idselection "svg")]
    
    (style idselection "width:650px; height:450px")
    
    (style svgselection "width:600px; height:400px")

    (let [idkey    (keyword id)
          selector (str "#" id " svg")
          datum    (sin-and-cos 100)
          config   {:xlabel  "Time (s)"
                    :xformat ",.1f"
                    :ylabel  "Voltage (v)"
                    :yformat ",.2f"}
          chart    (line-chart selector datum config)]

      (put-chart-state! idkey selector chart datum)
      
      (add-graph #(identity chart)))))

(defn update-chart
  "Update the chart with the id"
  [id]
  (let [idkey     (keyword id)
        new-obs   (sin-and-cos 1)
        old-data  (idkey @data)
        selector  (:selector old-data)
        chart     (:chart old-data)
        datum     (:datum old-data)
        update-fn #(assoc %1 :values (push-vec (:values %1) (first (:values %2))))
        new-datum (mapv update-fn datum new-obs)]
    
    (put-chart-state! idkey selector chart new-datum)
    
    (->
      (select selector)
      (.datum (clj->js new-datum)))
    
    (.update chart)
      
    nil))
