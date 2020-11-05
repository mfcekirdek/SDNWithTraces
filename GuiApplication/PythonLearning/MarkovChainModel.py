import numpy as np
import pandas
import pykov
import os

dir_path = os.path.dirname(os.path.realpath(__file__))


class Markov_Chain_Model(object):
    def __init__(self, csv_path):
        # hardcoded for now..
        self.data = pandas.read_csv(csv_path, sep=',', header=0).values
        self.cells = self.data[:, 5]
        self.N = len(np.unique(self.cells)) + 1
        trans_matrix = self.create_outgoing_transition_matrix(self.cells, self.N)
        i, d, t = self.findMostProbableStates(trans_matrix)
        self.most_probable_transition_matrix = d
        self.t = t
        self.A_matrix = np.zeros((self.N, self.N), dtype='int32')
        self.new_cell_counter = 0
        self.true_counter = 0
        self.total_counter = 0
        self.score_matrix = np.zeros((self.N, self.N), dtype='int32')
        self.change_threshold = 2
        self.where_inputs = []
        self.when_inputs = []
        self.predictions = []

    def create_outgoing_transition_matrix(self,cids, N):

        trans_matrix = np.zeros((N, N), dtype='int32')

        for i in range(0, len(cids) - 1):

            value = int(cids[i])
            next = int(cids[i + 1])

            while (next == 0):
                i = i + 1
                next = int(cids[i + 1])

            trans_matrix[value ][next] = trans_matrix[value][next ] + 1
        return trans_matrix
    def findMostProbableStates(self,a):

        result_i = np.zeros((len(a)), dtype='int32')
        result_d = np.zeros((len(a)), dtype='int32')
        result_t = np.zeros((len(a)), dtype='int32')

        for i in range(len(a)):
            t = max(a[i])
            d = np.argmax(a[i])
            result_d[i] = d
            result_i[i] = i
            result_t[i] = t

        return result_i, result_d, result_t

    def setFirstLocationAndWhen(self, first_location, first_when):
        self.current_cell = first_location
        self.current_when = first_when
        self.where_inputs.append(self.current_cell)
        self.when_inputs.append(self.current_when)

        self.counter_vector = pykov.Vector({self.current_cell: 1})
        self.normalized_vector = self.counter_vector.copy()
        self.T = pykov.Chain({(self.current_cell, self.current_cell): 1})
        self.learn_max_probable_cells_from_each_cells(self.most_probable_transition_matrix, self.t)


    def addNewCell(self, new_cell_num, prediction):
        self.counter_vector[new_cell_num] = self.counter_vector[new_cell_num] + 1
        normalized_vector = self.counter_vector.copy()
        normalized_vector.normalize()

        self.A_matrix[self.current_cell][new_cell_num] = self.A_matrix[self.current_cell][new_cell_num] + 1

        # if(prediction != new_cell_num):
        #     self.givePenalty(self.current_cell,new_cell_num,prediction)

        for i in range(self.N):
            if (np.sum(self.A_matrix[self.current_cell]) != 0):
                temp = self.A_matrix[self.current_cell][i] / np.sum(self.A_matrix[self.current_cell])
                self.T[(self.current_cell, i)] = temp
        self.current_cell = new_cell_num

    def getCellSize(self):
        return len(self.counter_vector)

    def predict(self):
        if (self.T.succ(self.current_cell)):
            result = self.T.move(self.current_cell)
            isNew = False
        else:
            self.new_cell_counter = self.new_cell_counter + 1
            isNew = True
            pred_list = self.T.pred(self.current_cell)
            result = -1
            for item in pred_list.items():
                if (self.counter_vector[item[0]] > result):
                    result = item[0]
        return result, isNew

    def getSpecificDays(self, int_day_nums, day_nums, cells, time_segments):

        cell_list = []
        dnum_list = []
        time_segment_list = []
        for i in range(len(int_day_nums)):
            d_num = int_day_nums[i]
            for j in range(len(day_nums)):
                if (day_nums[j] == d_num):
                    cell_list.append(cells[j])
                    time_segment_list.append(time_segments[j])
                    dnum_list.append(int_day_nums[i])

        return cell_list, time_segment_list, dnum_list

    def clearCounters(self):
        self.total_counter = 0
        self.true_counter = 0
        self.new_cell_counter = 0

    def printResults(self):
        print("True number : ", self.true_counter)
        print("Total number : ", self.total_counter)
        print("New cell number : ", self.new_cell_counter)
        print("Accuracy : ", (self.true_counter / (self.total_counter - self.new_cell_counter)))
        print("Accuracy_2 : ", (self.true_counter / (self.total_counter)))

    def learn_max_probable_cells_from_each_cells(self, matrix, t):
        for i in range(len(matrix)):
            self.A_matrix[i][matrix[i]] = t[i]
            self.T[(i, matrix[i])] = t[i]

    def givePenalty(self, prev, true, predicted):
        self.A_matrix[prev][predicted] = max(self.A_matrix[prev][predicted] - 1, 1)
        self.A_matrix[prev][true] = self.A_matrix[prev][true] + 1

    def predictAndLearn(self, next_real_where, next_when_input):
        ###############################################################################################################

        predicted_cell, isNew = self.predict()
        next_real_where = int(next_real_where)

        if (isNew):
            print("New Cell : ", next_real_where)

        if (next_real_where == predicted_cell):
            self.true_counter = self.true_counter + 1
            self.score_matrix[self.current_cell][predicted_cell] = 0
            result = True
        else:
            self.score_matrix[self.current_cell][predicted_cell] = self.score_matrix[self.current_cell][
                                                                       predicted_cell] + 1
            result = False

        self.total_counter = self.total_counter + 1

        if (self.score_matrix[self.current_cell][predicted_cell] >= self.change_threshold):
            tmp = self.A_matrix[self.current_cell][predicted_cell]
            self.A_matrix[self.current_cell][predicted_cell] = self.A_matrix[self.current_cell][next_real_where]
            self.A_matrix[self.current_cell][next_real_where] = tmp

        if ((self.current_cell, next_real_where) in self.T):
            self.addNewCell(next_real_where, predicted_cell)
        else:
            self.addNewCell(next_real_where, predicted_cell)

        # where_inputs.append(next_cell_prediction)
        self.where_inputs.append(next_real_where)
        self.when_inputs.append(next_when_input)
        self.predictions.append(predicted_cell)

        return result, str(next_real_where), str(predicted_cell), str(next_when_input)

    def clearHistoryStatistics(self):
        self.total_counter = 0
        self.true_counter = 0
        self.new_cell_counter = 0
        self.where_inputs.clear()
        self.when_inputs.clear()

    def getAccuracyScore(self):
        if (self.total_counter == 0):
            accuracy = 0
        else:
            accuracy = (self.true_counter / (self.total_counter))
        return self.true_counter, self.total_counter, accuracy

    def getArrays(self):
        return self.where_inputs, self.when_inputs, self.predictions
