<template>
  <div v-if="isAdmin"
       class="admin-view">
    <div class="admin-header">
      <h1>Admin Page</h1>
    </div>

    <b-card>
      <h4>Maintenance</h4>
      <b-form-group>
        <b-textarea v-model="maintenance.message"
                    class="mb-2"/>
        <b-row class="mb-2">
          <b-col>
            <span class="mr-2">Block Submit</span>
            <switches v-model="maintenance.submitBlocked"/>
          </b-col>
        </b-row>
        <b-button variant="primary"
                  class="mr-2"
                  :disabled="maintenanceStateLoading"
                  @click="setMaintenanceState">
          <loading v-if="maintenanceStateLoading"
                   message="Set"
                   :size="20"/>
          <span v-else>Set</span>
        </b-button>
        <b-button variant="primary"
                  class="mr-2"
                  :disabled="maintenanceStateLoading"
                  @click="resetMaintenanceState">
          <loading v-if="maintenanceStateLoading"
                   message="Reset"
                   :size="20"/>
          <span v-else>Reset</span>
        </b-button>
      </b-form-group>
      <h4>Statistics</h4>
      <div class="row g-3">
        <div class="col-md-6 col-sm-8">
          <label>From</label>
          <b-form-datepicker id="from-datepicker" viewMode="year" v-model="fromDate" class="mb-2 "></b-form-datepicker>
        </div>
        <div class="col-md-6 col-sm-8">
          <label>To</label>
          <b-form-datepicker id="to-datepicker" viewMode="year" v-model="toDate" class="mb-2"></b-form-datepicker>
        </div>
        <div class="col-md-6 col-sm-8">
          <div class="input-group-append">
            <b-button variant="primary"
                      class="mr-2"
                      @click="loadStatistics">
              Load Statistics
            </b-button>
          </div>
        </div>
      </div>

      <highcharts :options="chartOptions"
                  class="high-chart"/>
    </b-card>
  </div>
  <div v-else></div>
</template>

<script lang="ts">
import hasHTMLTitle from '@/mixins/hasHTMLTitle';
import {backendService} from '@/services/BackendService';
import {User} from '@/types/toolkit/auth';
import Loading from '@/components/utils/Loading.vue';
import Switches from 'vue-switches';
import {Statistics} from '@/types/toolkit/admin';
import moment from 'moment';

export default hasHTMLTitle.extend({
  name: 'AdminView',
  components: {
    Loading,
    Switches,
    // TODO: register highcharts locally instead of globally. But the local version creates an error (as seen in PlotTab.vue)
  },
  data() {
    return {
      maintenance: {
        message: this.$store.state.maintenance.message,
        submitBlocked: this.$store.state.maintenance.submitBlocked,
      },
      maintenanceStateLoading: false,
      fromDate: '',
      toDate: '',
      statistics: {} as Statistics
    };
  },
  computed: {
    chartOptions(): any {
      return {
        title: {
          text: 'Tool Stats',
        },
        xAxis: {
          title: {
            text: 'Tools',
          },
          categories: this.chartLabels,
        },
        yAxis: {
          title: {
            text: 'Tool count',
          },
        },
        tooltip: {
          formatter: function () {
            return `${this.x}: ${this.y}`;
          }
        },
        series: [{
          name: 'Total Tool Count',
          data: this.chartData,
        }],
        chart: {
          type: 'column',
        },
        credits: {
          enabled: false,
        },
      };
    },
    htmlTitle() {
      return 'Admin Page';
    },
    loggedIn(): boolean {
      return this.$store.getters['auth/loggedIn'];
    },
    user(): User | null {
      return this.$store.getters['auth/user'];
    },
    isAdmin(): boolean {
      return this.user !== null && this.user.isAdmin;
    },
    chartLabels(): string[] {
      if (this.statistics.totalToolStats) {
        return this.statistics.totalToolStats.singleToolStats.sort((a,b) => b.count - a.count).map(stats => stats.toolName);
      } else {
        return [];
      }
    },
    chartData(): number[] {
      if (this.statistics.totalToolStats) {
        return this.statistics.totalToolStats.singleToolStats.sort((a,b) => b.count - a.count).map(stats => stats.count);
      } else {
        return [];
      }
    }
  },
  methods: {
    setMaintenanceState(): void {
      this.maintenanceStateLoading = true;
      backendService.setMaintenanceState(this.maintenance).finally(() => {
        this.maintenanceStateLoading = false;
      });
    },
    loadStatistics(): void {
      const fromDate = this.fromDate === '' ? '1970-01-01' : this.fromDate
      const toDate = this.toDate === '' ? moment().format("YYYY-MM-DD") : this.toDate
      backendService.fetchStatistics(fromDate, toDate).then((statistics) => {
        console.log(statistics);
        this.statistics = statistics;
      });
    },
    resetMaintenanceState(): void {
      this.maintenance.message = '';
      this.maintenance.submitBlocked = false;
      this.setMaintenanceState();
    },
  },
});
</script>

<style lang="scss" scoped>
.admin-header {
  height: 2.75rem;

  h1 {
    color: $primary;
    font-weight: bold;
    font-size: 1.25em;
    line-height: 1.6;
  }
}
</style>
