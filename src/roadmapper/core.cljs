(ns roadmapper.core
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<! go]]
            [clojure.string :as s]))

(enable-console-print!)

(defmulti to-ms last)

(defmethod to-ms :days [[n & _]]
  (* n 24 60 60 1000))

(defn set-columns [dt]
  (doto dt
    (.addColumn "string" "Task ID")
    (.addColumn "string" "Task Name")
    (.addColumn "string" "Resource")
    (.addColumn "date" "Start Date")
    (.addColumn "date" "End Date")
    (.addColumn "number" "Duration")
    (.addColumn "number" "Percent Complete")
    (.addColumn "string" "Dependencies")))

(defn fetch-data []
  (go
    (let [{:keys [status body]} (<! (http/get "roadmap.edn"))]
      (if (= 200 status)
        body
        (throw (ex-info "Can't find roadmap.edn file" {}))))))

(defn description [{:keys [description resource]}]
  (if resource
    (str description " (" resource ")")
    resource))

(defn dependencies [{:keys [follows]}]
  (s/join "," follows))

(defn format-data [{:keys [tasks]}]
  (map (fn [{:keys [id group start end effort completed]
             :as task}]
         [id (description task) group nil nil (to-ms [3 :days]) 0 (dependencies task)])
       tasks))

(defn ^:export draw-chart []
  (go
    (let [chart (google.visualization.Gantt. (js/document.getElementById "chart_div"))
          data (<! (fetch-data))]
      (println (format-data data))
      (.draw chart
             (doto (google.visualization.DataTable.)
               set-columns
               (.addRows
                (clj->js (format-data data))))
             (clj->js {:height (* 55 (count (:tasks data)))})))))

(google.charts.load "current" (clj->js {:packages ["gantt"]}))
(google.charts.setOnLoadCallback draw-chart)
