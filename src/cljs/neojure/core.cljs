(ns neojure.core
  (:require [neojure.dom :as dom]))

;--------------------------------------------------------------------
; begin: description
; 
;     keep down complexity by specifying all datasets as a vector.
;     each item in the vector represents a series
;     each series can have [key, value(s), color] as keys.
;     key serves double function as a label.
;     if value key is used the model type is assumed a discrete chart type
;     continuous datasets are sometimes used in seemingly discrete situations
;     multibarchart is an example of a discrete ui that uses continuous data
;
; the goal is to have a key for every type of model object available in d3
;--------------------------------------------------------------------

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

(defn- colors
  []
  (->
    (js/d3.scale.category20)
    (.range)))

(defn line-chart-model
  "Create a line chart model."
  [] 
  (-> 
    (js/nv.models.lineChart)
    (.useInteractiveGuideline true)
    (.options (clj->js {:margin             {:left 50 :bottom 50}
                        :x                  (fn [d i] i)
                        :showXAxis          true
                        :showYAxis          true
                        :transitionDuration 250
                        :color              (colors)}))))

(defn pie-chart-model
  "Create a pie chart model."
  []
  (->
    (js/nv.models.pieChart)
    (.options (clj->js {:margin {:left 50 :bottom 50}
                        :x      (fn [d] (.-key d))
                        :y      (fn [d] (.-value d))
                        :color  (colors)}))))

(defn discrete-bar-chart-model
  "Create a bar chart model."
  []
  (->
    (js/nv.models.discreteBarChart)
    (.options (clj->js {:margin             {:left 50 :bottom 50}
                        :x                  (fn [d] (.-key d))
                        :y                  (fn [d] (.-value d))
                        :showValues         true
                        :transitionDuration 250
                        :color              (colors)}))))

(defn- make-model
  "Create the nvd3 model instanced based on the keyword identifier."
  [kw]
  (let [is? (partial keyword-identical? kw)]
    (cond
      (is? :line-chart)          (line-chart-model)
      (is? :pie-chart)           (pie-chart-model)
      (is? :discrete-bar-chart)  (discrete-bar-chart-model) 
      :else (throw (js/Error. "No valid chart type configured.")))))

(defn- render-chart
  "Create a line chart at the selector"
  [selector config]
  
  (let [model   (make-model (:type config))
        datum   (:datum config)
        options (:options config {})
        xaxis   (.-xAxis model)
        yaxis   (.-yAxis model)
        xlabel  (:xlabel options "X")
        ylabel  (:ylabel options "Y")
        has?    (partial contains? options)]
    
    (when (has? :xlabel)
      (.axisLabel xaxis (:xlabel options)))

    (when (has? :xformat)
      (.tickFormat xaxis (js/d3.format (:xformat options))))

    (when (has? :x-time-format)
      (.tickFormat xaxis (js/d3.time.format (:x-time-format options))))
    
    (when (has? :ylabel)
      (.axisLabel yaxis (:ylabel options)))

    (when (has? :yformat)
      (.tickFormat yaxis (js/d3.format (:yformat options))))

    (when (has? :y-time-format)
      (.tickFormat yaxis (js/d3.time.format (:y-time-format options))))
    
    (when (has? :valueFormat)
      (.valueFormat model (js/d3.format (:valueFormat options))))
    
    (->
      (dom/select selector)
      (.datum (clj->js datum))
      (.transition)
      (.duration 500)
      (.call model))

    (js/nv.utils.windowResize #(.update model))
    
    model))

(defn- assoc-by-key-type
  "Update discrete series if :value found else, assume :values"
  [to from]
  (if (contains? to :value)
    (assoc to :value (:value from))
    (assoc to :values (shiftv (:values to) (:values from)))))

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
        new-datum (mapv assoc-by-key-type datum new-obs)]
    
    (put-chart-state! idkey selector model new-datum)
    
    (->
      (dom/select selector)
      (.datum (clj->js new-datum)))
    
    (.update model)
  
    nil))
