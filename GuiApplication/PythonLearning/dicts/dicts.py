import csv

import pandas
import numpy as np

data = None
days = None
day_nums = None
u_days_dict = None
cells = None
time_segments = None
N = None
M = None

def init():

    global data
    global days
    global day_nums
    global u_days_dict
    global cells
    global time_segments
    global N
    global M

    data = pandas.read_csv("../new_data_with_num_days_nonzero.csv", sep=',', header=0)
    days = data.values[:, 0]
    day_nums = data.values[:, 9]
    cells = data.values[:, 5]
    time_segments = data.values[:, 11]
    N = len(np.unique(cells))
    M = 4
def getSpecificDays(int_day_nums,day_nums,cells,time_segments):

    cell_list = []
    dnum_list = []
    time_segment_list = []
    for i in range(len(int_day_nums)):
        d_num = int_day_nums[i]
        for j in range(len(day_nums)):
            if(day_nums[j] == d_num):
                cell_list.append(cells[j])
                time_segment_list.append(time_segments[j])
                dnum_list.append(int_day_nums[i])


    return cell_list, time_segment_list , dnum_list
def convertTimeSegmentToNumericValue(time_state):
    time_state_arr = ["A","E","M","N"]
    k = time_state_arr.index(time_state)
    return k
def create_outgoing_transition_matrix(cids,N):

    trans_matrix = np.zeros((N, N), dtype='int32')

    for i in range(0,len(cids)-1):

        value = int(cids[i])
        next = int(cids[i + 1])
        trans_matrix[value-1][next-1] = trans_matrix[value-1][next-1] + 1

    return trans_matrix
def create_initial_matrix(cids,N):

    initial_matrix = np.zeros((N), dtype='int32')

    for i in range(0, len(cids) - 1):

        value = int(cids[i])
        initial_matrix[value-1] = initial_matrix[value-1] + 1

    initial_matrix = initial_matrix / np.sum(initial_matrix)

    return initial_matrix
def create_B_matrix(cids,time_segments,N,M):
    m = np.zeros((N, M), dtype='int32')
    for i in range(len(time_segments)):
        m[cids[i]-1][convertTimeSegmentToNumericValue(time_segments[i])] = m[cids[i]-1][convertTimeSegmentToNumericValue(time_segments[i])] + 1

    m[2] = [1, 1, 1, 1]
    return m
def convert_number_to_symbols(num):

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
def make_B_Dict(list_B):

    dict_B = {}

    for i in range(len(list_B)):

        symbol_i = convert_number_to_symbols(i)

        for j in range(len(list_B[0])):
            categories = ["A", "E", "M" ,"N"]
            symbol_j = categories[j]
            key_i_to_j = symbol_i + symbol_j
            dict_B[key_i_to_j] = list_B[i][j]

    return dict_B
def make_PI_Dict(list_PI):
    dict_PI = {}

    for i in range(len(list_PI)):
        symbol_i = convert_number_to_symbols(i)
        dict_PI[symbol_i] = (list_PI[i])

    return dict_PI
def make_A_Dict(list_A):

    dict_A = {}

    for i in range(len(list_A)):

        symbol_i = convert_number_to_symbols(i)

        for j in range(len(list_A[0])):

            symbol_j = convert_number_to_symbols(j)
            key_i_to_j = symbol_i + symbol_j
            dict_A[key_i_to_j] = list_A[i][j]

    return dict_A
def write_dict_to_CSV(filename,dict):
    w = csv.writer(open(filename, "w"))
    for key, val in sorted(dict.items()):
        w.writerow([key, val])


init()
c,t,d = getSpecificDays([1,2],day_nums,cells,time_segments)

pi = create_initial_matrix(c,N)
a = create_outgoing_transition_matrix(c,N)
b = create_B_matrix(c,t,N,M)

dict_PI = make_PI_Dict(pi)
dict_A = make_A_Dict(a)
dict_B = make_B_Dict(b)

write_dict_to_CSV("dict_A.csv",dict_A)
write_dict_to_CSV("dict_B.csv",dict_B)
write_dict_to_CSV("dict_PI.csv",dict_PI)