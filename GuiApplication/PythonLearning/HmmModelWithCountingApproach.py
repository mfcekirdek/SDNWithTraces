import numpy as np
import os
import pandas


class HMM_Model(object):
    ''' Simple Hidden Markov Model implementation.  User provides
        transition, emission and initial probabilities in dictionaries
        mapping 2-character codes onto floating-point probabilities
        for those table entries.  States and emissions are represented
        with single characters.  Emission symbols comes from a finite.  '''

    def __init__(self, A, E, I, matrix_A, matrix_E, matrix_I, matrix_reverse_A, change_threshold):
        ''' Initialize the HMM given transition, emission and initial
            probability tables. '''
        self.change_threshold = change_threshold
        N = len(matrix_A)
        self.false_score_matrix = np.zeros((N, N), dtype='int32')

        # put state labels to the set self.Q
        self.Q, self.S = set(), set()  # states and symbols
        for a, prob in A.items():
            asrc, adst = a[0], a[1]
            asrc, adst = a[0:4], a[4:8]
            self.Q.add(asrc)
            self.Q.add(adst)
        # add all the symbols to the set self.S
        for e, prob in E.items():
            eq, es = e[0:4], e[4]
            self.Q.add(eq)
            self.S.add(es)

        self.Q = sorted(list(self.Q))
        self.S = sorted(list(self.S))

        # create maps from state labels / emission symbols to integers
        # that function as unique IDs
        qmap, smap = {}, {}
        for i in range(len(self.Q)): qmap[self.Q[i]] = i
        for i in range(len(self.S)): smap[self.S[i]] = i
        self.qmap, self.smap = qmap, smap

        self.matrix_A = matrix_A
        self.matrix_E = matrix_E
        self.matrix_I = matrix_I
        self.matrix_reverse_A = matrix_reverse_A

        a = self.matrix_A / self.matrix_A.sum(axis=1)[:, None]
        a = np.nan_to_num(a)

        e = self.matrix_E / self.matrix_E.sum(axis=1)[:, None]
        e = np.nan_to_num(e)

        i = self.matrix_I / np.sum(self.matrix_I)
        i = np.nan_to_num(i)

        rA = self.matrix_reverse_A / self.matrix_reverse_A.sum(axis=1)[:, None]
        rA = np.nan_to_num(rA)

        self.A = a
        self.E = e
        self.I = i
        self.rA = rA

        self.Alog = np.log2(self.A)
        self.Elog = np.log2(self.E)
        self.Ilog = np.log2(self.I)

    def dividePIntoSegments(self, p, s):

        result_list = []
        size = len(p)
        iteration = int(size / s)

        for i in range(iteration):
            k = i * s
            temp = p[k:k + s]
            result_list.append(temp)
        return result_list

    def jointProb(self, p, x, is_reverse):
        ''' Return joint probability of path p and emission string x '''
        # p = ['AAAA','AAAB','AAAA']
        p = self.dividePIntoSegments(p, 4)
        p = list(map(self.qmap.get, p))  # turn state characters into ids
        x = list(map(self.smap.get, x))  # turn emission characters into ids
        tot = self.I[p[0]]  # start with initial probability
        # p = [s-1 for s in p]
        for i in range(1, len(p)):
            if not (is_reverse):
                tot *= self.A[p[i - 1], p[i]]  # transition probability
            else:
                tot *= self.rA[p[i - 1], p[i]]  # transition probability
        for i in range(0, len(p)):
            tot *= self.E[p[i], x[i]]  # emission probability
        return tot



    def viterbi(self, x):
        ''' Given sequence of emissions, return the most probable path
            along with its probability. '''
        x = list(map(self.smap.get, x))  # turn emission characters into ids
        nrow, ncol = len(self.Q), len(x)
        mat = np.zeros(shape=(nrow, ncol), dtype=float)  # prob
        matTb = np.zeros(shape=(nrow, ncol), dtype=int)  # backtrace
        # Fill in first column
        for i in range(0, nrow):
            mat[i, 0] = self.E[i, x[0]] * self.I[i]
        # Fill in rest of prob and Tb tables
        for j in range(1, ncol):
            for i in range(0, nrow):
                ep = self.E[i, x[j]]
                mx, mxi = mat[0, j - 1] * self.A[0, i] * ep, 0
                for i2 in range(1, nrow):
                    pr = mat[i2, j - 1] * self.A[i2, i] * ep
                    if pr > mx:
                        mx, mxi = pr, i2
                mat[i, j], matTb[i, j] = mx, mxi
        # Find final state with maximal probability
        omx, omxi = mat[0, ncol - 1], 0
        for i in range(1, nrow):
            if mat[i, ncol - 1] > omx:
                omx, omxi = mat[i, ncol - 1], i
        # Backtrace
        i, p = omxi, [omxi]
        for j in range(ncol - 1, 0, -1):
            i = matTb[i, j]
            p.append(i)
        p = ''.join(map(lambda x: self.Q[x], p[::-1]))
        return omx, p  # Return probability and path

    def symbolsToNumbers(self, symbols):

        symbols = symbols.replace("A", "0")
        symbols = symbols.replace("B", "1")
        symbols = symbols.replace("C", "2")
        symbols = symbols.replace("D", "3")
        symbols = symbols.replace("E", "4")
        symbols = symbols.replace("F", "5")
        symbols = symbols.replace("G", "6")
        symbols = symbols.replace("H", "7")
        symbols = symbols.replace("I", "8")
        symbols = symbols.replace("J", "9")

        int_num = int(symbols)  # + 1

        return int_num

    def recalculateMatrixes(self, prev_state, next_state, time_segment, predicted_cell, is_new_cell):
        prev_state = self.symbolsToNumbers(prev_state)
        time_segment = ["A", "E", "M", "N"].index(time_segment)
        predicted_cell = self.symbolsToNumbers(predicted_cell)
        punishment_rate = 0.9

        self.matrix_A[prev_state][next_state] = self.matrix_A[prev_state][next_state] + 1
        self.matrix_E[next_state][time_segment] = self.matrix_E[next_state][time_segment] + 1
        self.matrix_I[next_state] = self.matrix_I[next_state] + 1
        self.matrix_reverse_A[next_state][prev_state] = self.matrix_reverse_A[next_state][prev_state] + 1

        if (is_new_cell):

            self.false_score_matrix[prev_state][predicted_cell] = self.false_score_matrix[prev_state][
                                                                      predicted_cell] + 1
            result = False
        else:
            if (predicted_cell == next_state):
                self.false_score_matrix[prev_state][predicted_cell] = 0
                # false_score_matrix[prev_state][predicted_cell] = max(false_score_matrix[prev_state][predicted_cell] - 1 , 0)
                result = True
            else:
                self.false_score_matrix[prev_state][predicted_cell] = self.false_score_matrix[prev_state][
                                                                          predicted_cell] + 1

                will_punished = True

                if (self.false_score_matrix[prev_state][predicted_cell] >= self.change_threshold):
                    ind = np.argmax(self.false_score_matrix[prev_state])
                    if (self.false_score_matrix[prev_state][ind] >= self.change_threshold):
                        tmp = self.matrix_A[prev_state][predicted_cell]
                        self.matrix_A[prev_state][predicted_cell] = self.matrix_A[prev_state][ind]
                        self.matrix_A[prev_state][ind] = tmp
                        will_punished = False

                if (will_punished):
                    temp1 = self.matrix_A[prev_state][predicted_cell]
                    self.matrix_A[prev_state][predicted_cell] = np.math.ceil(self.matrix_A[prev_state][predicted_cell] * (
                        punishment_rate ** self.false_score_matrix[prev_state][predicted_cell]))
                    diff = temp1 - self.matrix_A[prev_state][predicted_cell]
                    self.matrix_A[prev_state][next_state] = self.matrix_A[prev_state][next_state] + diff

                result = False

        if (self.false_score_matrix[prev_state][predicted_cell] >= self.change_threshold):
            ind = np.argmax(self.false_score_matrix[prev_state])
            if (self.false_score_matrix[prev_state][ind] >= self.change_threshold):
                tmp = self.matrix_A[prev_state][predicted_cell]
                self.matrix_A[prev_state][predicted_cell] = self.matrix_A[prev_state][ind]
                self.matrix_A[prev_state][ind] = tmp

        a = self.matrix_A / self.matrix_A.sum(axis=1)[:, None]
        a = np.nan_to_num(a)

        e = self.matrix_E / self.matrix_E.sum(axis=1)[:, None]
        e = np.nan_to_num(e)

        i = self.matrix_I / np.sum(self.matrix_I)
        i = np.nan_to_num(i)

        rA = self.matrix_reverse_A / self.matrix_reverse_A.sum(axis=1)[:, None]
        rA = np.nan_to_num(rA)

        self.A = a
        self.E = e
        self.I = i
        self.rA = rA

        return result

    def convertNumberToSymbols(self, num):

        str_num = '{0:04d}'.format(num)
        str_num = str_num.replace("0", "A")
        str_num = str_num.replace("1", "B")
        str_num = str_num.replace("2", "C")
        str_num = str_num.replace("3", "D")
        str_num = str_num.replace("4", "E")
        str_num = str_num.replace("5", "F")
        str_num = str_num.replace("6", "G")
        str_num = str_num.replace("7", "H")
        str_num = str_num.replace("8", "I")
        str_num = str_num.replace("9", "J")
        return str_num

    def calculatePathProb(self, p, x):
        where = ""
        when = ""
        for i in p:
            where = when + i
        for s in x:
            when = when + s
        prob = self.jointProb(where, when)
        return prob

    def calculateViterbi(self, arr):
        obs = ""
        for s in arr:
            obs = obs + s

        prob, path = self.viterbi(obs)

        return prob, path

    def printPathProb(self, path, prob):
        print("Prob : ", prob)
        print("Path : ")
        for i in range(len(path) - 1):
            print(path[(len(path) - 1)])

    def find_max_probable_cell_and_probability(self, previous_cells, previous_times, query_time, N):
        new_cell = True
        when = previous_times + query_time
        max_prob = 0
        result = previous_cells
        non_zero_cells = []
        non_zero_probs = []

        for i in range(N):
            cell_symbol = self.convertNumberToSymbols(i)
            where = previous_cells + cell_symbol
            prob = self.jointProb(where, when, False)
            if (prob != 0):
                new_cell = False
                non_zero_cells.append(self.symbolsToNumbers(cell_symbol))
                non_zero_probs.append(prob)

                if (prob > max_prob):
                    max_prob = prob
                    result = cell_symbol

        if (max_prob == 0):
            for i in range(N):
                cell_symbol = self.convertNumberToSymbols(i)
                where = cell_symbol + previous_cells
                prob = self.jointProb(where, when, True)

                if (prob != 0):
                    new_cell = False
                    non_zero_cells.append(self.symbolsToNumbers(cell_symbol))
                    non_zero_probs.append(prob)
                    if (prob > max_prob):
                        max_prob = prob
                        result = cell_symbol

        return result, max_prob, new_cell


class HMM_Learning_Object(object):
    def __init__(self, csv_path):

        self.data = None
        self.days = None
        self.day_nums = None
        self.u_days_dict = None
        self.cells = None
        self.time_segments = None
        self.N = None
        self.M = None
        self.alfa = 0.9
        self.last_transitions = None
        self.change_threshold = None
        self.hmm = None
        self.true_counter = 0
        self.total_counter = 0
        self.new_cell_counter = 0
        self.when_inputs = []
        self.where_inputs = []
        self.predictions = []
        self.current_location = None
        self.current_when = None
        self.change_threshold = 2
        self.data = pandas.read_csv(csv_path, sep=',', header=0)
        self.days = self.data.values[:, 0]
        self.day_nums = self.data.values[:, 9]
        self.cells = self.data.values[:, 5]
        self.time_segments = self.data.values[:, 11]
        self.N = len(np.unique(self.cells))
        self.M = 4
        self.last_transitions = np.zeros((self.N), dtype='int32')
        self.last_transitions = self.last_transitions - 1
        u_days = np.unique(self.days)
        u_days_dict = {}

        for i in range(len(u_days)):
            u_days_dict[u_days[i]] = i

    def setFirstLocationAndWhen(self, first_location, first_when):
        self.current_location = int(first_location) - 1
        self.current_when = first_when
        self.where_inputs.append(self.hmm.convertNumberToSymbols(self.current_location))
        self.when_inputs.append(self.current_when)

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

    def convertTimeSegmentToNumericValue(self, time_state):
        time_state_arr = ["A", "E", "M", "N"]
        k = time_state_arr.index(time_state)
        return k

    def create_outgoing_transition_matrix(self, cids, day_nums, alfa, N):
        current_day = day_nums[0]
        trans_matrix = np.zeros((N, N), dtype='int32')

        for i in range(0, len(cids) - 1):
            value = int(cids[i])
            next = int(cids[i + 1])
            trans_matrix[value - 1][next - 1] = trans_matrix[value - 1][next - 1] + 1
            self.last_transitions[value - 1] = next - 1
            if (day_nums[i + 1] != current_day):
                current_day = day_nums[i + 1]
                trans_matrix = trans_matrix * alfa

        return trans_matrix

    def create_incoming_adjacency_matrix(self, cids, N):

        reverse_adj_matrix = np.zeros((N, N), dtype='int32')

        for i in range(len(cids) - 1, 0, -1):

            value = int(cids[i])

            if (value == 0):
                continue

            previous = int(cids[i - 1])

            while (previous == 0):
                i = i - 1
                previous = int(cids[i - 1])
            reverse_adj_matrix[value - 1][previous - 1] = reverse_adj_matrix[value - 1][previous - 1] + 1

        return reverse_adj_matrix

    def create_initial_matrix(self, cids, day_nums, alfa, N):

        current_day = day_nums[0]

        initial_matrix = np.zeros((N), dtype='int32')

        for i in range(0, len(cids) - 1):
            value = int(cids[i])
            initial_matrix[value - 1] = initial_matrix[value - 1] + 1

            if (day_nums[i + 1] != current_day):
                current_day = day_nums[i + 1]
                initial_matrix = initial_matrix * alfa

        return initial_matrix

    def saveArraytoFile(self, dir, filename, array):
        np.save(dir + "/" + filename, array)

    def create_B_matrix(self, cids, day_nums, alfa, time_segments, N, M):
        current_day = day_nums[0]
        m = np.zeros((N, M), dtype='int32')

        for i in range(len(time_segments)):
            if (day_nums[i] != current_day):
                current_day = day_nums[i]
                m = m * alfa
            m[cids[i] - 1][self.convertTimeSegmentToNumericValue(time_segments[i])] = m[cids[i] - 1][
                                                                                          self.convertTimeSegmentToNumericValue(
                                                                                              time_segments[i])] + 1

        return m

    def train(self):

        matrix_A = np.zeros((self.N, self.N), dtype='int32')
        matrix_reverse_A = np.zeros((self.N, self.N), dtype='int32')
        matrix_I = np.ones((self.N), dtype='int32')
        matrix_E = np.ones((self.N, self.M), dtype='int32')

        def create_outgoing_transition_matrix2(cids, N):

            trans_matrix = np.zeros((N, N), dtype='int32')

            for i in range(0, len(cids) - 1):

                value = int(cids[i])

                if (value == 0):
                    continue

                next = int(cids[i + 1])

                while (next == 0):
                    i = i + 1
                    next = int(cids[i + 1])

                trans_matrix[value - 1][next - 1] = trans_matrix[value - 1][next - 1] + 1
            return trans_matrix

        a = create_outgoing_transition_matrix2(self.cells, self.N)

        def findMostProbableStates(a):

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

        i, d, t = findMostProbableStates(a)

        def learn_max_probable_cells_from_each_cells(matrix, t):
            for i in range(len(matrix)):
                matrix_A[i][matrix[i]] = t[i]

        learn_max_probable_cells_from_each_cells(d, t)

        dir_path = os.path.dirname(os.path.realpath(__file__))

        dic_PI = pandas.Series.from_csv(dir_path + "/dicts/dict_PI.csv", header=None).to_dict()
        dic_A = pandas.Series.from_csv(dir_path + "/dicts/dict_A.csv", header=None).to_dict()
        dic_B = pandas.Series.from_csv(dir_path + "/dicts/dict_B.csv", header=None).to_dict()

        hmm = HMM_Model(dic_A,  # transition matrix A
                        dic_B,  # emission matrix E
                        dic_PI, matrix_A, matrix_E, matrix_I, matrix_reverse_A,
                        self.change_threshold)  # initial probabilities I

        self.hmm = hmm
        return hmm

    def trainWithSpecificDays(self, training_days):

        c, t, d = self.getSpecificDays(training_days, self.day_nums, self.cells, self.time_segments)

        matrix_A = self.create_outgoing_transition_matrix(c, d, self.alfa, self.N)
        matrix_I = self.create_initial_matrix(c, d, self.alfa, self.N)
        matrix_E = self.create_B_matrix(c, d, self.alfa, t, self.N, self.M)
        matrix_reverse_A = self.create_incoming_adjacency_matrix(self.cells, self.N)

        dir_path = os.path.dirname(os.path.realpath(__file__))

        dic_PI = pandas.Series.from_csv(dir_path + "/dicts/dict_PI.csv", header=None).to_dict()
        dic_A = pandas.Series.from_csv(dir_path + "/dicts/dict_A.csv", header=None).to_dict()
        dic_B = pandas.Series.from_csv(dir_path + "/dicts/dict_B.csv", header=None).to_dict()

        hmm = HMM_Model(dic_A,  # transition matrix A
                        dic_B,  # emission matrix E
                        dic_PI, matrix_A, matrix_E, matrix_I, matrix_reverse_A,
                        self.change_threshold)  # initial probabilities I
        self.hmm = hmm
        return hmm

    def getAccuracyScore(self):
        if (self.total_counter == 0):
            accuracy = 0
        else:
            accuracy = (self.true_counter / (self.total_counter))
        return self.true_counter, self.total_counter, accuracy

    def clearHistoryStatistics(self):
        self.total_counter = 0
        self.true_counter = 0
        self.new_cell_counter = 0
        self.where_inputs.clear()
        self.when_inputs.clear()

    def getArrays(self):
        return self.where_inputs, self.when_inputs, self.predictions

    def predictAndLearn(self, next_real_where, next_when_input):

        ###############################################################################################################

        next_real_where = int(next_real_where)
        next_real_where = next_real_where - 1
        self.last_transitions[self.hmm.symbolsToNumbers(self.where_inputs[-1])] = next_real_where
        next_cell_prediction, prob, is_new_cell = self.hmm.find_max_probable_cell_and_probability(
            self.where_inputs[-1],
            self.when_inputs[-1],
            next_when_input, self.N)
        if (is_new_cell):
            tmp = next_cell_prediction + "_N"
            self.predictions.append(tmp)
        else:
            self.predictions.append(next_cell_prediction)

        result = self.hmm.recalculateMatrixes(self.where_inputs[-1], next_real_where, next_when_input,
                                              next_cell_prediction, is_new_cell)

        if self.where_inputs[-1] == next_cell_prediction:
            self.new_cell_counter = self.new_cell_counter + 1
        elif result:
            self.true_counter = self.true_counter + 1

        self.total_counter = self.total_counter + 1
        self.where_inputs.append(self.hmm.convertNumberToSymbols(next_real_where))
        self.when_inputs.append(next_when_input)

        return result, (next_real_where + 1), str(self.hmm.symbolsToNumbers(next_cell_prediction) + 1), \
               next_when_input
