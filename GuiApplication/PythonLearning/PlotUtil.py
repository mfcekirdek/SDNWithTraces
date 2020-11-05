import numpy as np
import matplotlib.pyplot as plt
import pandas


class Plotter(object):

    def __init__(self,predictionsFile):

        data = pandas.read_csv(predictionsFile, sep=',',header=0)
        self.time = data.values[:,0]
        self.hhmmss = data.values[:,1]
        self.realPositions = data.values[:,2]
        self.predictions = data.values[:,3]
        timeSegments = data.values[:,5]
        def convertTimeSegmentToNumericValue(time_state):
            time_state_arr = ["A", "E", "M", "N"]
            k = time_state_arr.index(time_state)
            return k

        self.timeSegments = [convertTimeSegmentToNumericValue(s) for s in timeSegments]

        hit_cell_indexes = []
        missed_cell_indexes = []

        for i in range(len(self.predictions)):
            if(self.predictions[i] == self.realPositions[i]):
                hit_cell_indexes.append(i)
            else:
                missed_cell_indexes.append(i)


        self.y_true = self.realPositions
        self.x_true = np.arange(1,len(self.timeSegments)+1,1)

        self.y_predicted = self.predictions
        self.x_predicted = np.arange(1,len(self.realPositions)+1,1)

        hit_cells = np.zeros((len(hit_cell_indexes)), dtype='int32')
        hit_time_segments = np.zeros((len(hit_cell_indexes)), dtype='int32')

        missed_cells = np.zeros((len(missed_cell_indexes)), dtype='int32')
        missed_predictions = np.zeros((len(missed_cell_indexes)), dtype='int32')
        missed_time_segments = np.zeros((len(missed_cell_indexes)), dtype='int32')




        for i in range(len(hit_cell_indexes)):
            hit_cells[i] = self.realPositions[hit_cell_indexes[i]]
            hit_time_segments[i] = hit_cell_indexes[i]

        for i in range(len(missed_cell_indexes)):
            missed_cells[i] = self.realPositions[missed_cell_indexes[i]]
            missed_predictions[i] = self.predictions[missed_cell_indexes[i]]
            missed_time_segments[i] = missed_cell_indexes[i]




    def plotRealMovement(self):
        fig, ax = plt.subplots()
        fig.canvas.draw()

        points_with_annotation = []
        for i in range(len(self.y_true)):
            point, = plt.plot(self.x_true[i], self.y_true[i], 'o-', color="b", markersize=10)
            dateInfo = "Day : {}, hhmmss : {}".format(self.time[i], self.hhmmss[i])
            cellInfo = "Cell Tower ID : {}".format(self.y_true[i])
            x_text = self.x_true[i]
            y_text = self.y_true[i]
            annotation = ax.annotate("{},{}".format(dateInfo, cellInfo),
                                     xy=(self.x_true[i], self.y_true[i]), xycoords='data',
                                     xytext=(x_text, y_text), textcoords='data',
                                     horizontalalignment="left",
                                     arrowprops=dict(arrowstyle="simple",
                                                     connectionstyle="arc3,rad=-0.2"),
                                     bbox=dict(boxstyle="round", facecolor="w",
                                               edgecolor="0.5", alpha=0.9)
                                     )
            # by default, disable the annotation visibility
            annotation.set_visible(False)

            points_with_annotation.append([point, annotation])

        def on_move(event):
            visibility_changed = False
            for point, annotation in points_with_annotation:
                should_be_visible = (point.contains(event)[0] == True)

                if should_be_visible != annotation.get_visible():
                    visibility_changed = True
                    annotation.set_visible(should_be_visible)

            if visibility_changed:
                plt.draw()

        ylim = (0, max(self.y_true) + 1)
        xlim = (0, len(self.x_true) + 1)

        if ylim is not None:
            plt.ylim(*ylim)
            plt.xlim(*xlim)
        plt.yticks(np.arange(1,max(10,max(self.y_true)+1), 1))
        plt.xticks(np.arange(1, len(self.x_true) + 1, 1))
        ax.set_xticklabels(self.hhmmss)

        plt.xlabel("HHMMSS")
        plt.ylabel("Cell Tower ID")

        plt.grid()

        plt.plot(self.x_true, self.y_true, 'o-', color="b",
                 label="Real Movement")

        plt.legend(loc="best")

        on_move_id = fig.canvas.mpl_connect('motion_notify_event', on_move)

        plt.show()
    def plotPredictedMovement(self):

        fig, ax = plt.subplots()
        fig.canvas.draw()

        points_with_annotation = []
        for i in range(len(self.y_predicted)):
            point, = plt.plot(self.x_predicted[i], self.y_predicted[i],  'o-', color="r", markersize=10)
            dateInfo = "Day : {}, hhmmss : {}".format(self.time[i],self.hhmmss[i])
            cellInfo = "Cell Tower ID : {}".format(self.y_predicted[i])
            x_text = self.x_predicted[i]
            y_text = self.y_predicted[i]

            annotation = ax.annotate("{},{}".format(dateInfo, cellInfo),
                                     xy=(self.x_predicted[i], self.y_predicted[i]), xycoords='data',
                                     xytext=(x_text, y_text), textcoords='data',
                                     horizontalalignment="left",
                                     arrowprops=dict(arrowstyle="simple",
                                                     connectionstyle="arc3,rad=-0.2"),
                                     bbox=dict(boxstyle="round", facecolor="w",
                                               edgecolor="0.5", alpha=0.9)
                                     )
            # by default, disable the annotation visibility
            annotation.set_visible(False)

            points_with_annotation.append([point, annotation])

        def on_move(event):
            visibility_changed = False
            for point, annotation in points_with_annotation:
                should_be_visible = (point.contains(event)[0] == True)

                if should_be_visible != annotation.get_visible():
                    visibility_changed = True
                    annotation.set_visible(should_be_visible)

            if visibility_changed:
                plt.draw()


        ylim=(0, max(self.y_predicted)+1)
        xlim=(0, len(self.x_predicted)+1)

        if ylim is not None:
            plt.ylim(*ylim)
            plt.xlim(*xlim)
        plt.yticks(np.arange(1, max(10,max(self.y_predicted)+1), 1))
        plt.xticks(np.arange(1,len(self.x_predicted)+1,1))
        ax.set_xticklabels(self.hhmmss)

        plt.xlabel("HHMMSS")
        plt.ylabel("Cell Tower ID")


        plt.grid()
        plt.plot(self.x_predicted,self.y_predicted, 'o-', color="r",
                 label="Predicted Movement")


        plt.legend(loc="best")


        on_move_id = fig.canvas.mpl_connect('motion_notify_event', on_move)





        plt.show()


# a = Plotter("predictions.csv")
# a.plotRealMovement()
# plotPredictedMovement()



