(ns neojure.frontend
  (:require [neojure.dom :as dom]))

; Realtime chart example found in nvd3 github test/realTimeChartTest.html

;--------------------------------------------------------------------
; begin: state
;
;     the data atom will hold all charts.
;     the first level key will be a key to the id on the page
;     the second keys are the selector, model, and datum
;--------------------------------------------------------------------

(def state (atom {}))

;--------------------------------------------------------------------
; end: state
;--------------------------------------------------------------------

(defn- shiftv
  "Push out as many items in from and concat to end of to"
  [to from]
  (-> 
    (drop (count from) to)
    (concat from)
    (vec)))

(defn- add-graph
  "nvd3 rendering queue"
  [f]
  (js/nv.addGraph f))

(defn- put-chart-state!
  "Update the state atom" 
  [idkey selector model datum]
  (let [set-chart #(identity {:selector selector
                              :model    model
                              :datum    datum})]
    (swap! state #(update-in % [idkey] set-chart))
    
    nil))

(defn- line-chart-model
  "Create a line chart model with sensible defaults
   TODO how can this reuse the options?"
  [] 
  (-> 
    (js/nv.models.lineChart)
    (.useInteractiveGuideline true)
    (.options (clj->js {:margin             {:left 50 :bottom 50}
                        :x                  (fn [d i] i)
                        :showXAxis          true
                        :showYAxis          true
                        :transitionDuration 250}))))

(defn- make-model
  "Create the nvd3 model instanced based on the keyword identifier."
  [kw]
  (cond
    (keyword-identical? kw :line-chart) (line-chart-model)
    :else (throw (js/Error. "No valid chart type configured."))))

(defn- render-chart
  "Create a line chart at the selector"
  [selector config]
  
  (let [model   (make-model (:type config))
        datum   (:datum config)
        options (:options config)
        xaxis   (.-xAxis model)
        yaxis   (.-yAxis model)]
    
    (when (contains? options :xlabel)
      (.axisLabel xaxis (:xlabel options)))

    (when (contains? options :xformat)
      (.tickFormat xaxis (js/d3.format (:xformat options))))

    (when (contains? options :ylabel)
      (.axisLabel yaxis (:ylabel options)))

    (when (contains? options :yformat)
      (.tickFormat yaxis (js/d3.format (:yformat options))))

    (->
      (dom/select selector)
      (.datum (clj->js datum))
      (.transition)
      (.duration 500)
      (.call model))

    (js/nv.utils.windowResize #(.update model))
    
    model))

(defn create
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

(defn update
  "Update and render the chart <id> with a new observation.
   An observaton is defined identically to the series.
   The series will bes hifted by the amount found in the new series.
   This means if the new len >= old len, it uses the entire new set."
  [id new-obs]
  (let [idkey     (keyword id)
        old-data  (idkey @state)
        selector  (:selector old-data)
        model     (:model old-data)
        datum     (:datum old-data)
        update-fn #(assoc %1 :values (shiftv (:values %1) (:values %2)))
        new-datum (mapv update-fn datum new-obs)]
    
    (put-chart-state! idkey selector model new-datum)
    
    (->
      (dom/select selector)
      (.datum (clj->js new-datum)))
    
    (.update model)
  
    nil))
