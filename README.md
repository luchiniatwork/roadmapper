# Roadmapper

Have beautiful timelines and roadmaps in minutes.

## Requirements

- [Clojure](https://clojure.org/guides/install_clojure)

## Getting started

Within the project folder, simply run:

``` shell
$ clojure -m figwheel.main -b dev
```

This should trigger your browser to point to
`http://localhost:9500`. If it doesn't, manually point your brower to
this URL.

Within the folder `resources/public` you will see a `roadmap.json`
file. That's where your raw roadmap data will go.

Once you change it, save it, and refresh the page on your browser and
the roadmap will be updated there.

## roadmap.json format

The main node in this file is `tasks` which is an array of tasks. Each
task represents a row within the roadmap.

``` json
{
  "tasks": [
    {
      "id": "task1",
      "description": "task by end time",
      "resource": "people",
      "group": "1",
      "start": "Q3/2023",
      "end": "2023/07/16"
    },
    {
      "id": "task2",
      "description": "task by effort",
      "resource": "people",
      "group": "1",
      "follows": ["task1"],
      "effort": "3 weeks"
    },
    {
      "id": "task3",
      "description": "task by effort another group",
      "resource": "people",
      "group": "1",
      "follows": ["task1"],
      "effort": "5 weeks"
    }
  ]
}
```

Each task must have:

- an `"id"` representing a unique name for this task
- a `"description"` representing the description that will show on the
  respective row of the roadmap
- a `"resource"`: who or which team will be tackling this task
- a temporal placement (see below)

Optionally, you can specify a `"group"` field that will be used to
visually group tasks by color (helping to visualize different lanes of
work for instance.)

### Temporal placement

Tasks must be placed within a temporal placement in the bigger scheme
of things. The most important thing is specifying when the task
starts. The options are:

- have a `"start"` field specifying when (as in "the calendar when")
  this task starts (see below for how to specify dates)
- have a `"follows"` field specifying which tasks should happen prior
  to this task (in array of task ids). The code will calculate when
  tasks marked as "follows" can start the earliest

In the example below, `taskA` starts on the 3rd quarter of 2023 and
ends by the end of the 4th quarter.

``` json
{
  "id": "taskA",
  "description": "this is task A",
  "resource": "jon",
  "start": "Q3/2023",
  "end": "Q4/2023"
}
```

In the next example, `taskB` starts only after `taskA` is finished and
still ends by the end of the 4th quarter.

``` json
{
  "id": "taskB",
  "description": "this is task B",
  "resource": "jon",
  "follows": ["taskA"],
  "end": "Q4/2023"
}
```

### Lenght of time/effort

When it comes to establishing when the task should be done you have
two options:

- have an `"end"` field specifying when (as in "the calendar when")
  this task ends (see below for how to specify dates)
- have an `"effort"` field specifying the amount of time required for
  the task (see below for how to specify effort)

See examples on the session above for how `"end"` works.

In the following example, `taskC` starts on the 12th of May and takes
6 weeks:

``` json
{
  "id": "taskC",
  "description": "this is task B",
  "resource": "jon",
  "starts": "2023/05/12",
  "effort": "6 weeks"
}
```
### Specifying dates

Dates can be:

- usual `YYYY/MM/DD` format (or shorter `yy/mm/dd`.) i.e. `2025/02/16`
  is equivalent to `25/2/16`
- `<quarter>/YYYY` (or `<quarter>/yy`) where `<quarter>` is one of
  `Q1`, `Q2`, `Q3`, or `Q4` representing the quarters of the
  year. i.e. `Q2/25`
- `<month>/YYYY` (or `<month>/yy`) where `<month>` is one of `jan`,
  `feb` through `dec`. i.e. `sep/1978`

### Specifying effort

Effort can be specifed in any of `quarters`, `months`, `weeks`, and
`days` (singular versions are also supported) in natural human
way. i.e. `4 months`, `12 weeks`, `2 quarters`, `1 day` are all valid
efforts
