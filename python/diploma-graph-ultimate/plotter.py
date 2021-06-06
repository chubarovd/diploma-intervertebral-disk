import matplotlib.pyplot as plt


def main():
    ps1 = []
    ps2 = []
    ps3 = []
    ps4 = []
    xs = []

    with open('../../java/pressures_log.txt') as data_file:
        data_rows = data_file.readlines()
        i = 0.0
        #data_rows = data_rows[:240]
        for row in data_rows:
            vals = row.replace(' ', '').replace(',', '.').split('|')
            ps1.append(float(vals[0]))
            ps2.append(float(vals[1]))
            ps3.append(float(vals[2]))
            ps4.append(float(vals[3]))
            xs.append(i / 60)
            i += 1

    plt.rc('font', size=12)
    plt.rc('axes', titlesize=5)
    plt.rc('figure', figsize=(9, 7))
    line_width = 2

    fig, axs = plt.subplots(2, 2)
    axs[0, 0].plot(xs, ps1, 'tab:blue', linewidth=line_width)
    axs[1, 0].plot(xs, ps2, 'tab:orange', linewidth=line_width)
    axs[0, 1].plot(xs, ps3, 'tab:green', linewidth=line_width)
    axs[1, 1].plot(xs, ps4, 'tab:red', linewidth=line_width)

    plt.setp(axs[:, :], ylabel='pressure, kPa', xlabel='time, min')

    fig.tight_layout()
    plt.show()

    data_file.close()


if __name__ == "__main__":
    main()
