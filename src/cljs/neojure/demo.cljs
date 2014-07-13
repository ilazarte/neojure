(ns neojure.demo
  (:require [neojure.dom  :as dom]
            [neojure.core :as nj]))

(defn- sin
  [val]
  (js/Math.sin val))

(defn- cos
  [val]
  (js/Math.cos val))

(defn- random
  []
  (js/Math.random))

(defn sin-and-cos-dataset
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

(defn upward-dataset
  "Produce values for use in a bar chart"
  [max]
  (let [nums (range 1 max)
        robj  #(identity {:x % :y (* % (random))})
        vals  #(map robj nums)]
    [{:key   "Motorola"
      :values (vals)}
     {:key   "Samsung"
      :values (vals)}
     {:key   "LG"
      :values (vals)}]))

(defn single-value-dataset
  "Produce a chart of single values"
  []
  [{:key "Facebook"
    :value (random)}
   {:key "Google"
    :value (random)}
   {:key "Apple"
    :value (random)}
   {:key "Microsoft"
    :value (random)}
   {:key "Cisco"
    :value (random)}
   {:key "Intel"
    :value (random)}
   {:key "IBM"
    :value (random)}])

(defn discrete-bar-chart-dataset
  "Produce a chart of single values"
  []
  [{:key    "Market Share"
    :values [{:key "Facebook"
              :value (random)}
             {:key "Google"
              :value (random)}
             {:key "Apple"
              :value (random)}
             {:key "Microsoft"
              :value (random)}
             {:key "Cisco"
              :value (random)}
             {:key "Intel"
              :value (random)}
             {:key "IBM"
              :value (random)}]}])

(defn set-elements
  "Assert the div and svg container elements"
  [id]
  (let [bodyselection (dom/select "body")
        idselection   (dom/put-by-id bodyselection "div" id)
        svgselection  (dom/put-by-tag idselection "svg")]
    (dom/style idselection "width:650px; height:450px")
    (dom/style svgselection "width:600px; height:400px")))

;--------------------------------------------------------------------
; demo functions
;--------------------------------------------------------------------

(defn create-demo-line-chart
  [id]
  (set-elements id) 
  (nj/create id {:type    :line-chart
                 :datum   (sin-and-cos-dataset 100)
                 :options {:xlabel  "Time (s)"
                           :xformat ",.1f"
                           :ylabel  "Voltage (v)"
                           :yformat ",.2f"}}))

(defn update-demo-line-chart
  [id]
  (nj/update id (sin-and-cos-dataset 1)))

(defn create-demo-pie-chart
  [id]
  (set-elements id)
  (nj/create id {:type    :pie-chart
                 :datum   (single-value-dataset)
                 :options {:valueFormat ",.2f"}}))

(defn update-demo-pie-chart
  [id]
  (nj/update id (single-value-dataset)))

(defn create-demo-discrete-bar-chart
  [id]
  (set-elements id)
  (nj/create id {:type  :discrete-bar-chart
                 :datum (discrete-bar-chart-dataset)}))

(defn update-demo-discrete-bar-chart
  [id]
  (nj/update id (discrete-bar-chart-dataset)))
