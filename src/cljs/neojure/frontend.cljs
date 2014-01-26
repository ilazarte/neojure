(ns neojure.frontend
  (:require [neojure.dom :as dom]))

; Realtime chart example found in nvd3 github test/realTimeChartTest.html

;--------------------------------------------------------------------
; begin: state
;
;     the data atom will hold all charts.
;     the first level key will be a key to the id on the page
;     the second keys are the selector, chart instance, and data
;--------------------------------------------------------------------

(def state (atom {}))

;--------------------------------------------------------------------
; end: state
;--------------------------------------------------------------------

; TODO vec operations imply a protocol to me

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
  "Pop the head add the new elem at the end"
  [coll elem]
  (-> coll rest vec (conj elem)))

(defn- add-graph [f]
  (js/nv.addGraph f))

(defn- put-chart-state!
  [idkey selector model datum]
  (let [set-chart #(identity {:selector selector
                              :model    model
                              :datum    datum})]
    (swap! state #(update-in % [idkey] set-chart))
    
    nil))

(defn- render-chart
  "Create a line chart at the selector"
  [selector config]
  
  (let [model (:model config)
        datum (:datum config)
        axes  (:axes config)]
    (-> 
      (.-xAxis model)
      (.axisLabel (:xlabel axes))
      (.tickFormat (js/d3.format (:xformat axes))))
    
    (-> 
      (.-yAxis model)
      (.axisLabel (:ylabel axes))
      (.tickFormat (js/d3.format (:yformat axes))))
    
    (->
      (dom/select selector)
      (.datum (clj->js datum))
      (.transition)
      (.duration 500)
      (.call model))

    (js/nv.utils.windowResize #(.update model))
    
    model))

(defn line-chart-model
  
  "Create a line chart model.
   No args is reasonable defaults
   Interactive lnie chart is available on the model by default"
  
  ([] (line-chart-model {:margin             {:left 50 :bottom 50}
                         :x                  (fn [d i] i)
                         :showXAxis          true
                         :showYAxis          true
                         :transitionDuration 250})) 
  ([opts]
    (-> 
        (js/nv.models.lineChart)
        (.useInteractiveGuideline true)
        (.options (clj->js opts)))))

(defn create-chart
  
  "Create and render a new chart with <id> and config
   The element div#<id> should exist already.
   The config should contain keys axes, datum, model"
  
  [id config]
  
  (let [idkey    (keyword id)
        selector (str "#" id " svg")
        datum    (:datum config)
        model    (render-chart selector config)]

      (put-chart-state! idkey selector model datum)
      
      (add-graph #(identity model))))

(defn update-chart
  
  "Update and render the chart <id> with a new observation.
   An observaton is defined identically to the series,
   but only has one data point"
  
  [id new-obs]
  
  (let [idkey     (keyword id)
        old-data  (idkey @state)
        selector  (:selector old-data)
        model     (:model old-data)
        datum     (:datum old-data)
        update-fn #(assoc %1 :values (push-vec (:values %1) (first (:values %2))))
        new-datum (mapv update-fn datum new-obs)]
    
    (put-chart-state! idkey selector model new-datum)
    
    (->
      (dom/select selector)
      (.datum (clj->js new-datum)))
    
    (.update model)
    
    nil))
