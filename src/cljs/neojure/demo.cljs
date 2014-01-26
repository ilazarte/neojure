(ns neojure.demo
  (:require [neojure.dom      :as dom]
            [neojure.frontend :as frontend]))

(defn- sin
  [val]
  (js/Math.sin val))

(defn- cos
  [val]
  (js/Math.cos val))

(defn- random
  []
  (js/Math.random))

(defn sin-and-cos-series
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

(defn create-demo-line-chart
  "Create a a line chart by adding a few dom elements first"
  [id]
  (let [bodyselection (dom/select "body")
        idselection   (dom/put-by-id bodyselection "div" id)
        svgselection  (dom/put-by-tag idselection "svg")
        config        {:datum (sin-and-cos-series 100)
                       :model (frontend/line-chart-model)
                       :axes  {:xlabel  "Time (s)"
                               :xformat ",.1f"
                               :ylabel  "Voltage (v)"
                               :yformat ",.2f"}} ]
    
    (dom/style idselection "width:650px; height:450px")
    (dom/style svgselection "width:600px; height:400px")
   
    (frontend/create-chart id config)))

(defn update-demo-line-chart
  [id]
  (frontend/update-chart id (sin-and-cos-series 1)))
